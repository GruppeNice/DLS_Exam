package com.dlsexam.streamingservice.dto;

import com.dlsexam.streamingservice.domain.PlaybackStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class StreamingDtos {

    private StreamingDtos() {
    }

    public record StartPlaybackRequest(
        @NotNull UUID contentId
    ) {
    }

    public record UpdateProgressRequest(
        @Min(0) long positionSeconds
    ) {
    }

    public record PlaybackSessionResponse(
        UUID id,
        UUID userId,
        UUID contentId,
        PlaybackStatus status,
        long positionSeconds,
        String drmToken,
        Instant startedAt,
        Instant stoppedAt,
        Instant resumedAt,
        Instant updatedAt
    ) {
    }
}
