package com.dlsexam.billingservice.messaging;

import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BillingEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public BillingEventPublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.messaging.exchange}") String exchange
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void subscriptionActivated(UUID subscriptionId, UUID userId, UUID planId, String planCode, Instant occurredAt) {
        publish("subscription.activated", new SubscriptionActivatedEvent(subscriptionId, userId, planId, planCode, occurredAt));
    }

    public void subscriptionCancelled(UUID subscriptionId, UUID userId, UUID planId, String reason, Instant occurredAt) {
        publish("subscription.cancelled", new SubscriptionCancelledEvent(subscriptionId, userId, planId, reason, occurredAt));
    }

    public void paymentSucceeded(UUID paymentId, UUID userId, long amountCents, String currency, Instant occurredAt) {
        publish("payment.succeeded", new PaymentSucceededEvent(paymentId, userId, amountCents, currency, occurredAt));
    }

    public void paymentFailed(UUID paymentId, UUID userId, String reason, Instant occurredAt) {
        publish("payment.failed", new PaymentFailedEvent(paymentId, userId, reason, occurredAt));
    }

    private void publish(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public record SubscriptionActivatedEvent(
        UUID subscriptionId,
        UUID userId,
        UUID planId,
        String planCode,
        Instant occurredAt
    ) {
    }

    public record SubscriptionCancelledEvent(
        UUID subscriptionId,
        UUID userId,
        UUID planId,
        String reason,
        Instant occurredAt
    ) {
    }

    public record PaymentSucceededEvent(
        UUID paymentId,
        UUID userId,
        long amountCents,
        String currency,
        Instant occurredAt
    ) {
    }

    public record PaymentFailedEvent(
        UUID paymentId,
        UUID userId,
        String reason,
        Instant occurredAt
    ) {
    }
}
