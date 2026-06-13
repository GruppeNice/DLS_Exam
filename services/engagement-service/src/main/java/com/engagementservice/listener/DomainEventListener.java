package com.engagementservice.listener;

import com.engagementservice.config.RabbitConfig;
import com.engagementservice.service.DomainNotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DomainEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DomainEventListener.class);

    private final DomainNotificationService domainNotificationService;
    private final ObjectMapper objectMapper;

    public DomainEventListener(DomainNotificationService domainNotificationService, ObjectMapper objectMapper) {
        this.domainNotificationService = domainNotificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfig.DOMAIN_EVENTS_QUEUE)
    public void handleDomainEvent(Message message) {
        try {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), new TypeReference<>() {
            });

            switch (routingKey) {
                case "subscription.activated" -> domainNotificationService.handleSubscriptionActivated(payload);
                case "playback.stopped" -> domainNotificationService.handlePlaybackStopped(payload);
                case "content.created" -> domainNotificationService.handleContentCreated(payload);
                default -> logger.debug("Ignoring unsupported domain event: {}", routingKey);
            }
        } catch (Exception exception) {
            logger.warn("Failed to process domain event: {}", exception.getMessage());
        }
    }
}
