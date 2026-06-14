package com.engagementservice.listener;

import com.engagementservice.config.RabbitConfig;
import com.engagementservice.service.DomainNotificationService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class DomainEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DomainEventListener.class);

    private final DomainNotificationService domainNotificationService;

    public DomainEventListener(DomainNotificationService domainNotificationService) {
        this.domainNotificationService = domainNotificationService;
    }

    @RabbitListener(queues = RabbitConfig.DOMAIN_EVENTS_QUEUE)
    public void handleDomainEvent(
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
        @Payload Map<String, Object> payload
    ) {
        try {
            switch (routingKey) {
                case "subscription.activated" -> domainNotificationService.handleSubscriptionActivated(payload);
                case "playback.stopped" -> domainNotificationService.handlePlaybackStopped(payload);
                case "content.created" -> domainNotificationService.handleContentCreated(payload);
                case "content.reviewed" -> domainNotificationService.handleContentReviewed(payload);
                case "review.voted" -> domainNotificationService.handleReviewVoted(payload);
                default -> logger.debug("Ignoring unsupported domain event: {}", routingKey);
            }
        } catch (Exception exception) {
            logger.warn("Failed to process domain event: {}", exception.getMessage());
        }
    }
}
