package com.engagementservice.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEmailResolver {

    private final ConcurrentHashMap<UUID, String> emailsByUserId = new ConcurrentHashMap<>();
    private final UUID demoUserId;
    private final String demoUserEmail;

    public UserEmailResolver(
        @Value("${app.engagement.demo-user-id:dddddddd-dddd-dddd-dddd-ddddddddddd1}") UUID demoUserId,
        @Value("${app.engagement.demo-user-email:demo@dls.local}") String demoUserEmail
    ) {
        this.demoUserId = demoUserId;
        this.demoUserEmail = demoUserEmail;
        emailsByUserId.put(demoUserId, demoUserEmail);
    }

    public void remember(UUID userId, String email) {
        if (userId != null && email != null && !email.isBlank()) {
            emailsByUserId.put(userId, email.trim().toLowerCase());
        }
    }

    public String resolve(UUID userId) {
        if (demoUserId.equals(userId)) {
            return demoUserEmail;
        }
        return emailsByUserId.getOrDefault(userId, userId + "@users.dls.local");
    }
}
