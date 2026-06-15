package com.engagementservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void handleReviewVotedEmailsAuthorOnUpvote() {
        UUID voterUserId = UUID.randomUUID();
        UUID reviewAuthorId = UUID.randomUUID();
        when(userEmailResolver.resolve(reviewAuthorId)).thenReturn("author@example.com");

        domainNotificationService.handleReviewVoted(Map.of(
            "userId", voterUserId.toString(),
            "reviewAuthorId", reviewAuthorId.toString(),
            "reviewId", UUID.randomUUID().toString(),
            "value", 1,
            "reviewText", "Great movie"
        ));

        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).queueNotification(requestCaptor.capture());
        NotificationRequest request = requestCaptor.getValue();
        assertEquals("author@example.com", request.getRecipient());
        assertEquals("review-upvoted", request.getTemplateName());
    }

    @Test
    void handleReviewVotedSkipsDownvote() {
        domainNotificationService.handleReviewVoted(Map.of(
            "userId", UUID.randomUUID().toString(),
            "reviewAuthorId", UUID.randomUUID().toString(),
            "reviewId", UUID.randomUUID().toString(),
            "value", -1
        ));

        verifyNoInteractions(notificationService);
    }

    @Test
    void handleReviewVotedSkipsSelfUpvote() {
        UUID userId = UUID.randomUUID();
        domainNotificationService.handleReviewVoted(Map.of(
            "userId", userId.toString(),
            "reviewAuthorId", userId.toString(),
            "reviewId", UUID.randomUUID().toString(),
            "value", 1
        ));

        verifyNoInteractions(notificationService);
    }
}
