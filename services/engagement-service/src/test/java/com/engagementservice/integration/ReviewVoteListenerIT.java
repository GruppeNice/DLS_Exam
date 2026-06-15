package com.engagementservice.integration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.engagementservice.dto.NotificationRequest;
import com.engagementservice.service.NotificationService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test: verifies the full AMQP flow from a published "review.voted" message to
 * DomainEventListener routing it through DomainNotificationService and ultimately calling
 * NotificationService.queueNotification(). Uses a real RabbitMQ container — only
 * NotificationService is mocked to avoid DB/mail side-effects.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ReviewVoteListenerIT {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    /** Mock only the final sink — listener, routing, and domain service logic are all real. */
    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void resetMock() throws InterruptedException {
        Mockito.clearInvocations(notificationService);
        Thread.sleep(500);
    }

    @Test
    void upvoteMessageRoutedThroughListenerTriggersNotification() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        rabbitTemplate.convertAndSend("review.events", "review.voted", Map.of(
            "reviewId", reviewId.toString(),
            "userId", userId.toString(),
            "value", 1,
            "occurredAt", Instant.now().toString()
        ));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            verify(notificationService, atLeastOnce())
                .queueNotification(any(NotificationRequest.class))
        );
    }

    @Test
    void downvoteMessageRoutedThroughListenerTriggersNotification() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        rabbitTemplate.convertAndSend("review.events", "review.voted", Map.of(
            "reviewId", reviewId.toString(),
            "userId", userId.toString(),
            "value", -1
        ));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            verify(notificationService, atLeastOnce())
                .queueNotification(any(NotificationRequest.class))
        );
    }

    @Test
    void voteMessageWithMissingUserIdIsIgnoredGracefully() {
        // When userId is absent, handleReviewVoted should return early without queuing
        rabbitTemplate.convertAndSend("review.events", "review.voted", Map.of(
            "reviewId", UUID.randomUUID().toString(),
            "value", 1
        ));

        // Give enough time for the listener to pick up and process the message
        await().pollDelay(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(6)).untilAsserted(() ->
            Mockito.verifyNoInteractions(notificationService)
        );
    }
}
