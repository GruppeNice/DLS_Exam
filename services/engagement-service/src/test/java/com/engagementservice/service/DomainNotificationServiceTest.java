package com.engagementservice.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.engagementservice.dto.NotificationRequest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DomainNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserEmailResolver userEmailResolver;

    private DomainNotificationService domainNotificationService;

    @BeforeEach
    void setUp() {
        domainNotificationService = new DomainNotificationService(notificationService, userEmailResolver, "demo@dls.local");
    }

    @Test
    void handleReviewVotedBuildsUpvoteNotification() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        when(userEmailResolver.resolve(userId)).thenReturn("user@example.com");

        domainNotificationService.handleReviewVoted(Map.of(
            "userId", userId.toString(),
            "reviewId", reviewId.toString(),
            "value", 1
        ));

        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).queueNotification(requestCaptor.capture());
        String reviewText = String.valueOf(requestCaptor.getValue().getTemplateVariables().get("reviewText"));
        assertTrue(reviewText.contains("upvote"));
    }

    @Test
    void handleReviewVotedBuildsDownvoteNotification() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        when(userEmailResolver.resolve(userId)).thenReturn("user@example.com");

        domainNotificationService.handleReviewVoted(Map.of(
            "userId", userId.toString(),
            "reviewId", reviewId.toString(),
            "value", -1
        ));

        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).queueNotification(requestCaptor.capture());
        String reviewText = String.valueOf(requestCaptor.getValue().getTemplateVariables().get("reviewText"));
        assertTrue(reviewText.contains("downvote"));
    }
}

