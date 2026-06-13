import logging
import threading

import pika
from sqlalchemy.orm import Session, sessionmaker

from recommendation_service.config import Settings
from recommendation_service.messaging.event_handlers import EventHandler, parse_message

logger = logging.getLogger(__name__)


class RabbitMqConsumer:
    def __init__(
        self,
        settings: Settings,
        session_factory: sessionmaker,
        event_handler: EventHandler | None = None,
    ):
        self.settings = settings
        self.session_factory = session_factory
        self.event_handler = event_handler or EventHandler()
        self._thread: threading.Thread | None = None
        self._stopping = threading.Event()

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._stopping.clear()
        self._thread = threading.Thread(target=self._run, name="rabbitmq-consumer", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stopping.set()

    def _run(self) -> None:
        while not self._stopping.is_set():
            try:
                self._consume_once()
            except Exception:
                logger.exception("RabbitMQ consumer failure; retrying in 5 seconds")
                self._stopping.wait(5)

    def _consume_once(self) -> None:
        credentials = pika.PlainCredentials(self.settings.rabbitmq_username, self.settings.rabbitmq_password)
        params = pika.ConnectionParameters(
            host=self.settings.rabbitmq_host,
            port=self.settings.rabbitmq_port,
            credentials=credentials,
            heartbeat=30,
            blocked_connection_timeout=30,
        )

        connection = pika.BlockingConnection(params)
        channel = connection.channel()

        queue_name = "recommendation-service.events"
        channel.queue_declare(queue=queue_name, durable=True)

        bindings = [
            (self.settings.streaming_events_exchange, "playback.started"),
            (self.settings.streaming_events_exchange, "playback.progress.updated"),
            (self.settings.streaming_events_exchange, "playback.stopped"),
            (self.settings.billing_events_exchange, "subscription.activated"),
            (self.settings.review_events_exchange, "content.rated"),
        ]

        for exchange, routing_key in bindings:
            channel.exchange_declare(exchange=exchange, exchange_type="topic", durable=True)
            channel.queue_bind(queue=queue_name, exchange=exchange, routing_key=routing_key)

        def on_message(ch, method, properties, body):  # noqa: ANN001
            db: Session = self.session_factory()
            try:
                payload = parse_message(body)
                self.event_handler.handle(db, method.routing_key, payload)
                ch.basic_ack(delivery_tag=method.delivery_tag)
            except Exception:
                logger.exception("Failed to process event with routing key %s", method.routing_key)
                ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
            finally:
                db.close()

        channel.basic_qos(prefetch_count=10)
        channel.basic_consume(queue=queue_name, on_message_callback=on_message)
        logger.info("RabbitMQ consumer started on queue %s", queue_name)

        try:
            channel.start_consuming()
        except KeyboardInterrupt:
            channel.stop_consuming()
        finally:
            connection.close()
