package com.dlsexam.userservice.dto;

import com.dlsexam.userservice.domain.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(min = 2, max = 120) String displayName
    ) {
    }

    public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {
    }

    public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserProfileResponse user
    ) {
    }

    public record UserProfileResponse(
        UUID id,
        String email,
        String displayName,
        AccountStatus status,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
    ) {
    }

    public record SuspendUserRequest(
        @NotBlank String reason
    ) {
    }

    public record PasswordResetRequest(
        @Email @NotBlank String email
    ) {
    }
}
