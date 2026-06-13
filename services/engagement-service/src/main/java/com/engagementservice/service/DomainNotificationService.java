package com.engagementservice.service;

import com.engagementservice.dto.NotificationRequest;
import com.engagementservice.types.NotificationType;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DomainNotificationService {

    private final NotificationService notificationService;
    private final UserEmailResolver userEmailResolver;
    private final String broadcastRecipient;

    public DomainNotificationService(
        NotificationService notificationService,
        UserEmailResolver userEmailResolver,
        @Value("${app.engagement.broadcast-recipient:demo@dls.local}") String broadcastRecipient
    ) {
        this.notificationService = notificationService;
        this.userEmailResolver = userEmailResolver;
        this.broadcastRecipient = broadcastRecipient;
    }

    public void handleSubscriptionActivated(Map<String, Object> payload) {
        UUID userId = parseUuid(payload.get("userId"));
        if (userId == null) {
            return;
        }

        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.EMAIL);
        request.setRecipient(userEmailResolver.resolve(userId));
        request.setSubject("Your DLS subscription is active");
        request.setTemplateName("subscription-activated");
        request.setTemplateVariables(Map.of(
            "planCode", String.valueOf(payload.getOrDefault("planCode", "BASIC")),
            "userName", userEmailResolver.resolve(userId)
        ));
        notificationService.queueNotification(request);
    }

    public void handlePlaybackStopped(Map<String, Object> payload) {
        UUID userId = parseUuid(payload.get("userId"));
        UUID contentId = parseUuid(payload.get("contentId"));
        if (userId == null || contentId == null) {
            return;
        }

        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.EMAIL);
        request.setRecipient(userEmailResolver.resolve(userId));
        request.setSubject("Continue watching on DLS");
        request.setTemplateName("continue-watching");
        request.setTemplateVariables(Map.of(
            "name", userEmailResolver.resolve(userId),
            "contentId", contentId.toString(),
            "positionSeconds", payload.getOrDefault("positionSeconds", 0)
        ));
        notificationService.queueNotification(request);
    }

    public void handleContentCreated(Map<String, Object> payload) {
        String title = String.valueOf(payload.getOrDefault("title", "New title"));
        UUID contentId = parseUuid(payload.get("contentId"));

        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.EMAIL);
        request.setRecipient(broadcastRecipient);
        request.setSubject("New on DLS: " + title);
        request.setTemplateName("new-content");
        request.setTemplateVariables(Map.of(
            "title", title,
            "contentType", payload.getOrDefault("contentType", "MOVIE"),
            "contentId", contentId == null ? "" : contentId.toString()
        ));
        notificationService.queueNotification(request);
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
        return UUID.fromString(value.toString());
    }
}
