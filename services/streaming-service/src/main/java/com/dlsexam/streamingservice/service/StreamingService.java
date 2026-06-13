package com.dlsexam.streamingservice.service;

import com.dlsexam.streamingservice.client.SubscriptionValidator;
import com.dlsexam.streamingservice.domain.PlaybackSession;
import com.dlsexam.streamingservice.domain.PlaybackStatus;
import com.dlsexam.streamingservice.dto.StreamingDtos.PlaybackSessionResponse;
import com.dlsexam.streamingservice.dto.StreamingDtos.StartPlaybackRequest;
import com.dlsexam.streamingservice.dto.StreamingDtos.UpdateProgressRequest;
import com.dlsexam.streamingservice.messaging.PlaybackEventPublisher;
import com.dlsexam.streamingservice.repository.PlaybackSessionRepository;
import com.dlsexam.streamingservice.service.DrmValidator.DrmResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StreamingService {

    private final PlaybackSessionRepository sessionRepository;
    private final SubscriptionValidator subscriptionValidator;
    private final DrmValidator drmValidator;
    private final PlaybackEventPublisher eventPublisher;

    public StreamingService(
        PlaybackSessionRepository sessionRepository,
        SubscriptionValidator subscriptionValidator,
        DrmValidator drmValidator,
        PlaybackEventPublisher eventPublisher
    ) {
        this.sessionRepository = sessionRepository;
        this.subscriptionValidator = subscriptionValidator;
        this.drmValidator = drmValidator;
        this.eventPublisher = eventPublisher;
    }

    public List<PlaybackSessionResponse> listUserSessions(UUID userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    public PlaybackSessionResponse getSession(UUID userId, UUID sessionId) {
        return toResponse(findOwnedSession(userId, sessionId));
    }

    @Transactional
    public PlaybackSessionResponse startPlayback(UUID userId, StartPlaybackRequest request, String idempotencyKey) {
        return sessionRepository.findByIdempotencyKey(idempotencyKey)
            .map(existing -> {
                if (!existing.getUserId().equals(userId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency key already used by another user");
                }
                return toResponse(existing);
            })
            .orElseGet(() -> createPlaybackSession(userId, request.contentId(), idempotencyKey));
    }

    @Transactional
    public PlaybackSessionResponse stopPlayback(UUID userId, UUID sessionId) {
        PlaybackSession session = findOwnedSession(userId, sessionId);
        if (session.getStatus() != PlaybackStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not active");
        }

        session.setStatus(PlaybackStatus.STOPPED);
        session.setStoppedAt(Instant.now());
        PlaybackSession saved = sessionRepository.save(session);
        eventPublisher.playbackStopped(
            saved.getId(),
            saved.getUserId(),
            saved.getContentId(),
            saved.getPositionSeconds(),
            Instant.now()
        );
        return toResponse(saved);
    }

    @Transactional
    public PlaybackSessionResponse resumePlayback(UUID userId, UUID sessionId) {
        PlaybackSession session = findOwnedSession(userId, sessionId);
        if (session.getStatus() == PlaybackStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed sessions cannot be resumed");
        }

        subscriptionValidator.requireActiveSubscription(userId);

        DrmResult drmResult = drmValidator.validate(userId, session.getContentId());
        if (!drmResult.valid()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, drmResult.reason());
        }

        session.setStatus(PlaybackStatus.ACTIVE);
        session.setDrmToken(drmResult.token());
        session.setResumedAt(Instant.now());
        session.setStoppedAt(null);
        PlaybackSession saved = sessionRepository.save(session);
        eventPublisher.playbackStarted(
            saved.getId(),
            saved.getUserId(),
            saved.getContentId(),
            saved.getPositionSeconds(),
            Instant.now()
        );
        return toResponse(saved);
    }

    @Transactional
    public PlaybackSessionResponse updateProgress(UUID userId, UUID sessionId, UpdateProgressRequest request) {
        PlaybackSession session = findOwnedSession(userId, sessionId);
        if (session.getStatus() != PlaybackStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Progress can only be updated for active sessions");
        }

        session.setPositionSeconds(request.positionSeconds());
        PlaybackSession saved = sessionRepository.save(session);
        eventPublisher.playbackProgressUpdated(
            saved.getId(),
            saved.getUserId(),
            saved.getContentId(),
            saved.getPositionSeconds(),
            Instant.now()
        );
        return toResponse(saved);
    }

    private PlaybackSessionResponse createPlaybackSession(UUID userId, UUID contentId, String idempotencyKey) {
        subscriptionValidator.requireActiveSubscription(userId);

        DrmResult drmResult = drmValidator.validate(userId, contentId);
        if (!drmResult.valid()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, drmResult.reason());
        }

        PlaybackSession session = new PlaybackSession();
        session.setUserId(userId);
        session.setContentId(contentId);
        session.setStatus(PlaybackStatus.ACTIVE);
        session.setPositionSeconds(0);
        session.setDrmToken(drmResult.token());
        session.setIdempotencyKey(idempotencyKey);
        session.setStartedAt(Instant.now());
        PlaybackSession saved = sessionRepository.save(session);
        eventPublisher.playbackStarted(
            saved.getId(),
            saved.getUserId(),
            saved.getContentId(),
            saved.getPositionSeconds(),
            Instant.now()
        );
        return toResponse(saved);
    }

    private PlaybackSession findOwnedSession(UUID userId, UUID sessionId) {
        PlaybackSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playback session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Playback session does not belong to user");
        }
        return session;
    }

    private PlaybackSessionResponse toResponse(PlaybackSession session) {
        return new PlaybackSessionResponse(
            session.getId(),
            session.getUserId(),
            session.getContentId(),
            session.getStatus(),
            session.getPositionSeconds(),
            session.getDrmToken(),
            session.getStartedAt(),
            session.getStoppedAt(),
            session.getResumedAt(),
            session.getUpdatedAt()
        );
    }
}
