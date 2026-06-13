package com.dlsexam.streamingservice.repository;

import com.dlsexam.streamingservice.domain.PlaybackSession;
import com.dlsexam.streamingservice.domain.PlaybackStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaybackSessionRepository extends JpaRepository<PlaybackSession, UUID> {

    Optional<PlaybackSession> findByIdempotencyKey(String idempotencyKey);

    List<PlaybackSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<PlaybackSession> findByUserIdAndContentIdAndStatus(UUID userId, UUID contentId, PlaybackStatus status);
}
