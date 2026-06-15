package com.dlsexam.streamingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dlsexam.streamingservice.client.SubscriptionValidator;
import com.dlsexam.streamingservice.domain.PlaybackSession;
import com.dlsexam.streamingservice.domain.PlaybackStatus;
import com.dlsexam.streamingservice.dto.StreamingDtos.StartPlaybackRequest;
import com.dlsexam.streamingservice.dto.StreamingDtos.UpdateProgressRequest;
import com.dlsexam.streamingservice.messaging.PlaybackEventPublisher;
import com.dlsexam.streamingservice.repository.PlaybackSessionRepository;
import com.dlsexam.streamingservice.service.DrmValidator.DrmResult;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StreamingServiceTest {

    @Mock
    private PlaybackSessionRepository sessionRepository;

    @Mock
    private SubscriptionValidator subscriptionValidator;

    @Mock
    private DrmValidator drmValidator;

    @Mock
    private PlaybackEventPublisher eventPublisher;

    private StreamingService streamingService;

    @BeforeEach
    void setUp() {
        streamingService = new StreamingService(sessionRepository, subscriptionValidator, drmValidator, eventPublisher);
    }

    @Test
    void startPlayback_createsNewSession_whenNoPreviousSessionExists() {
        UUID userId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        String idempotencyKey = "idem-1";

        when(sessionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(sessionRepository.findFirstByUserIdAndContentIdOrderByUpdatedAtDesc(userId, contentId)).thenReturn(Optional.empty());
        when(drmValidator.validate(userId, contentId)).thenReturn(DrmResult.success("drm-token"));
        when(sessionRepository.save(any(PlaybackSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = streamingService.startPlayback(userId, new StartPlaybackRequest(contentId), idempotencyKey);

        assertEquals(userId, response.userId());
        assertEquals(contentId, response.contentId());
        assertEquals(PlaybackStatus.ACTIVE, response.status());
        assertEquals(0L, response.positionSeconds());
        assertEquals("drm-token", response.drmToken());
        verify(subscriptionValidator).requireActiveSubscription(userId);
        verify(drmValidator).validate(userId, contentId);
        verify(eventPublisher).playbackStarted(any(), eq(userId), eq(contentId), eq(0L), any(Instant.class));
    }

    @Test
    void startPlayback_returnsExistingSession_whenIdempotencyKeyBelongsToSameUser() {
        UUID userId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        String idempotencyKey = "idem-2";
        PlaybackSession existing = buildSession(userId, contentId, PlaybackStatus.ACTIVE, 42L);

        when(sessionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

        var response = streamingService.startPlayback(userId, new StartPlaybackRequest(contentId), idempotencyKey);

        assertEquals(PlaybackStatus.ACTIVE, response.status());
        assertEquals(42L, response.positionSeconds());
        verify(sessionRepository, never()).save(any());
        verify(subscriptionValidator, never()).requireActiveSubscription(any());
        verify(eventPublisher, never()).playbackStarted(any(), any(), any(), anyLong(), any());
    }

    @Test
    void startPlayback_throwsConflict_whenIdempotencyKeyBelongsToDifferentUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        String idempotencyKey = "idem-3";
        PlaybackSession existing = buildSession(otherUserId, contentId, PlaybackStatus.ACTIVE, 10L);

        when(sessionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> streamingService.startPlayback(userId, new StartPlaybackRequest(contentId), idempotencyKey)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void stopPlayback_throwsConflict_whenSessionIsNotActive() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        PlaybackSession stoppedSession = buildSession(userId, contentId, PlaybackStatus.STOPPED, 15L);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(stoppedSession));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> streamingService.stopPlayback(userId, sessionId, 20L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void updateProgress_savesNewPositionAndPublishesEvent_forActiveSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        PlaybackSession activeSession = buildSession(userId, contentId, PlaybackStatus.ACTIVE, 5L);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionRepository.save(activeSession)).thenReturn(activeSession);

        var response = streamingService.updateProgress(userId, sessionId, new UpdateProgressRequest(120L));

        assertEquals(120L, response.positionSeconds());
        verify(eventPublisher).playbackProgressUpdated(any(), eq(userId), eq(contentId), eq(120L), any(Instant.class));
    }

    private PlaybackSession buildSession(UUID userId, UUID contentId, PlaybackStatus status, long positionSeconds) {
        PlaybackSession session = new PlaybackSession();
        session.setUserId(userId);
        session.setContentId(contentId);
        session.setStatus(status);
        session.setPositionSeconds(positionSeconds);
        session.setStartedAt(Instant.now());
        return session;
    }
}

