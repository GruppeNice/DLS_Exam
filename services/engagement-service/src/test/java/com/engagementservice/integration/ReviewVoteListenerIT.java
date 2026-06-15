package com.engagementservice.integration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
        UUID voterUserId = UUID.randomUUID();
        UUID reviewAuthorId = UUID.randomUUID();

        rabbitTemplate.convertAndSend("review.events", "review.voted", Map.of(
            "reviewId", UUID.randomUUID().toString(),
            "userId", voterUserId.toString(),
            "reviewAuthorId", reviewAuthorId.toString(),
            "value", 1,
            "reviewText", "Great movie",
            "occurredAt", Instant.now().toString()
        ));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            verify(notificationService, atLeastOnce())
                .queueNotification(any(NotificationRequest.class))
        );
    }

    @Test
    void downvoteMessageDoesNotTriggerNotification() {
        rabbitTemplate.convertAndSend("review.events", "review.voted", Map.of(
            "reviewId", UUID.randomUUID().toString(),
            "userId", UUID.randomUUID().toString(),
            "reviewAuthorId", UUID.randomUUID().toString(),
            "value", -1
        ));

        await().pollDelay(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(6)).untilAsserted(() ->
            verifyNoInteractions(notificationService)
        );
    }

    @Test
    void voteMessageWithMissingAuthorIdIsIgnoredGracefully() {
        rabbitTemplate.convertAndSend("review.events", "review.voted", Map.of(
            "reviewId", UUID.randomUUID().toString(),
            "userId", UUID.randomUUID().toString(),
            "value", 1
        ));

        await().pollDelay(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(6)).untilAsserted(() ->
            verifyNoInteractions(notificationService)
        );
    }
}
