package com.engagementservice.listener;

import static org.mockito.Mockito.verify;

import com.engagementservice.service.DomainNotificationService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DomainEventListenerTest {

    @Mock
    private DomainNotificationService domainNotificationService;

    @Test
    void handleDomainEventRoutesReviewVotedToNotificationService() {
        DomainEventListener listener = new DomainEventListener(domainNotificationService);
        Map<String, Object> payload = Map.of("reviewId", "11111111-1111-1111-1111-111111111111");

        listener.handleDomainEvent("review.voted", payload);

        verify(domainNotificationService).handleReviewVoted(payload);
    }
}

