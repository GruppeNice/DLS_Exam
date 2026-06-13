package com.ratingandreviewservice.messaging;

import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public ReviewEventPublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.messaging.exchange}") String exchange
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void contentRated(UUID ratingId, UUID userId, UUID contentId, int stars, Instant occurredAt) {
        publish("content.rated", new ContentRatedEvent(ratingId, userId, contentId, stars, occurredAt));
    }

    public void contentReviewed(UUID reviewId, UUID userId, UUID contentId, Instant occurredAt) {
        publish("content.reviewed", new ContentReviewedEvent(reviewId, userId, contentId, occurredAt));
    }

    private void publish(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public record ContentRatedEvent(
        UUID ratingId,
        UUID userId,
        UUID contentId,
        int stars,
        Instant occurredAt
    ) {
    }

    public record ContentReviewedEvent(
        UUID reviewId,
        UUID userId,
        UUID contentId,
        Instant occurredAt
    ) {
    }
}
