package com.ratingandreviewservice.messaging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ratingandreviewservice.dto.ReviewVotesRequest;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.repository.ReviewRepository;
import com.ratingandreviewservice.service.ReviewVotesService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test: verifies that calling ReviewVotesService.addReviewVote() results in a real
 * AMQP message being published to the review.events exchange with routing key "review.voted".
 * Uses an embedded RabbitMQ container via Testcontainers — no mocks in the messaging layer.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ReviewVoteMessagingIT {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    @Autowired
    private ReviewVotesService reviewVotesService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private static final String EXCHANGE = "review.events";
    private static final String ROUTING_KEY = "review.voted";

    /** Creates a unique auto-delete queue and binds it to the exchange to capture messages per test. */
    private String bindCaptureQueue() {
        String queueName = "test.capture." + UUID.randomUUID();
        Queue queue = new Queue(queueName, false, false, true);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(
            BindingBuilder.bind(queue).to(new TopicExchange(EXCHANGE)).with(ROUTING_KEY));
        return queueName;
    }

    @Test
    void upvotingAReviewPublishesReviewVotedMessageToRabbitMQ() {
        String captureQueue = bindCaptureQueue();

        Review review = savedReview();
        reviewVotesService.addReviewVote(new ReviewVotesRequest(UUID.randomUUID(), review.getId(), 1));

        Message message = rabbitTemplate.receive(captureQueue, 5_000);
        assertNotNull(message, "Expected a review.voted AMQP message after upvote — none arrived within 5 s");
        assertTrue(new String(message.getBody()).contains("\"value\":1"),
            "Message body should carry value:1 for an upvote");
    }

    @Test
    void downvotingAReviewPublishesReviewVotedMessageToRabbitMQ() {
        String captureQueue = bindCaptureQueue();

        Review review = savedReview();
        reviewVotesService.addReviewVote(new ReviewVotesRequest(UUID.randomUUID(), review.getId(), -1));

        Message message = rabbitTemplate.receive(captureQueue, 5_000);
        assertNotNull(message, "Expected a review.voted AMQP message after downvote — none arrived within 5 s");
        assertTrue(new String(message.getBody()).contains("\"value\":-1"),
            "Message body should carry value:-1 for a downvote");
    }

    private Review savedReview() {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setUserId(UUID.randomUUID());
        review.setMovieId(UUID.randomUUID());
        review.setCreatedAt(LocalDate.now());
        return reviewRepository.save(review);
    }
}



