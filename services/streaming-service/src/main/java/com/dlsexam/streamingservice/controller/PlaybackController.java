package com.dlsexam.streamingservice.controller;

import com.dlsexam.streamingservice.dto.StreamingDtos.PlaybackSessionResponse;
import com.dlsexam.streamingservice.dto.StreamingDtos.StartPlaybackRequest;
import com.dlsexam.streamingservice.dto.StreamingDtos.UpdateProgressRequest;
import com.dlsexam.streamingservice.security.UserPrincipal;
import com.dlsexam.streamingservice.service.StreamingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/playback")
public class PlaybackController {

    private final StreamingService streamingService;

    public PlaybackController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @GetMapping("/sessions/me")
    public List<PlaybackSessionResponse> mySessions(@AuthenticationPrincipal UserPrincipal principal) {
        return streamingService.listUserSessions(principal.getId());
    }

    @GetMapping("/sessions/{sessionId}")
    public PlaybackSessionResponse getSession(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID sessionId
    ) {
        return streamingService.getSession(principal.getId(), sessionId);
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaybackSessionResponse start(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody StartPlaybackRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return streamingService.startPlayback(principal.getId(), request, requireIdempotencyKey(idempotencyKey));
    }

    @PostMapping("/sessions/{sessionId}/stop")
    public PlaybackSessionResponse stop(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID sessionId
    ) {
        return streamingService.stopPlayback(principal.getId(), sessionId);
    }

    @PostMapping("/sessions/{sessionId}/resume")
    public PlaybackSessionResponse resume(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID sessionId
    ) {
        return streamingService.resumePlayback(principal.getId(), sessionId);
    }

    @PutMapping("/sessions/{sessionId}/progress")
    public PlaybackSessionResponse updateProgress(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID sessionId,
        @Valid @RequestBody UpdateProgressRequest request
    ) {
        return streamingService.updateProgress(principal.getId(), sessionId, request);
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        return idempotencyKey.trim();
    }
}
