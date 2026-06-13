package com.dlsexam.streamingservice.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DrmValidator {

    private final double simulatedFailureRate;

    public DrmValidator(@Value("${app.drm.simulated-failure-rate}") double simulatedFailureRate) {
        this.simulatedFailureRate = simulatedFailureRate;
    }

    public DrmResult validate(UUID userId, UUID contentId) {
        if (Math.random() < simulatedFailureRate) {
            return DrmResult.failure("Simulated DRM validation failure");
        }
        String token = "drm_sim_" + userId.toString().replace("-", "").substring(0, 8)
            + "_" + contentId.toString().replace("-", "").substring(0, 8);
        return DrmResult.success(token);
    }

    public record DrmResult(boolean valid, String token, String reason) {

        public static DrmResult success(String token) {
            return new DrmResult(true, token, null);
        }

        public static DrmResult failure(String reason) {
            return new DrmResult(false, null, reason);
        }
    }
}
