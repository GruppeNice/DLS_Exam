package com.dlsexam.streamingservice.messaging;

import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlaybackEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public PlaybackEventPublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.messaging.exchange}") String exchange
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void playbackStarted(UUID sessionId, UUID userId, UUID contentId, long positionSeconds, Instant occurredAt) {
        publish("playback.started", new PlaybackStartedEvent(sessionId, userId, contentId, positionSeconds, occurredAt));
    }

    public void playbackStopped(UUID sessionId, UUID userId, UUID contentId, long positionSeconds, Instant occurredAt) {
        publish("playback.stopped", new PlaybackStoppedEvent(sessionId, userId, contentId, positionSeconds, occurredAt));
    }

    public void playbackProgressUpdated(
        UUID sessionId,
        UUID userId,
        UUID contentId,
        long positionSeconds,
        Instant occurredAt
    ) {
        publish(
            "playback.progress.updated",
            new PlaybackProgressUpdatedEvent(sessionId, userId, contentId, positionSeconds, occurredAt)
        );
    }

    private void publish(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public record PlaybackStartedEvent(
        UUID sessionId,
        UUID userId,
        UUID contentId,
        long positionSeconds,
        Instant occurredAt
    ) {
    }

    public record PlaybackStoppedEvent(
        UUID sessionId,
        UUID userId,
        UUID contentId,
        long positionSeconds,
        Instant occurredAt
    ) {
    }

    public record PlaybackProgressUpdatedEvent(
        UUID sessionId,
        UUID userId,
        UUID contentId,
        long positionSeconds,
        Instant occurredAt
    ) {
    }
}
