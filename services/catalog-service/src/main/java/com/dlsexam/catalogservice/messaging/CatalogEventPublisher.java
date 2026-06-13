package com.dlsexam.catalogservice.messaging;

import com.dlsexam.catalogservice.domain.ContentType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CatalogEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public CatalogEventPublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.messaging.catalog-exchange}") String exchange
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void contentCreated(UUID contentId, String title, ContentType contentType, Instant occurredAt) {
        publish("content.created", new ContentCreatedEvent(contentId, title, contentType.name(), occurredAt));
    }

    public void contentUpdated(UUID contentId, String title, ContentType contentType, Instant occurredAt) {
        publish("content.updated", new ContentUpdatedEvent(contentId, title, contentType.name(), occurredAt));
    }

    public void contentRemoved(UUID contentId, Instant occurredAt) {
        publish("content.removed", new ContentRemovedEvent(contentId, occurredAt, true));
    }

    private void publish(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public record ContentCreatedEvent(UUID contentId, String title, String contentType, Instant occurredAt) {
    }

    public record ContentUpdatedEvent(UUID contentId, String title, String contentType, Instant occurredAt) {
    }

    public record ContentRemovedEvent(UUID contentId, Instant occurredAt, boolean tombstone) {
    }
}
