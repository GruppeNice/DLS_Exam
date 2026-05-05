package com.dlsexam.userservice.messaging;

import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public UserEventPublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.messaging.exchange}") String exchange
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void userRegistered(UUID userId, String email) {
        publish("user.registered", new UserRegisteredEvent(userId, email, Instant.now()));
    }

    public void userSuspended(UUID userId, String email, String reason) {
        publish("user.suspended", new UserSuspendedEvent(userId, email, reason, Instant.now()));
    }

    public void userDeleted(UUID userId, String email) {
        publish("user.deleted", new UserDeletedEvent(userId, email, Instant.now()));
    }

    private void publish(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public record UserRegisteredEvent(UUID userId, String email, Instant occurredAt) {
    }

    public record UserSuspendedEvent(UUID userId, String email, String reason, Instant occurredAt) {
    }

    public record UserDeletedEvent(UUID userId, String email, Instant occurredAt) {
    }
}
