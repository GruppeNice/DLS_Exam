package com.dlsexam.streamingservice.client;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SubscriptionValidator {

    private final RestClient restClient;

    public SubscriptionValidator(@Value("${app.billing-service.base-url}") String billingBaseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(billingBaseUrl)
            .build();
    }

    public void requireActiveSubscription(UUID userId) {
        ActiveSubscriptionResponse response = restClient.get()
            .uri("/api/v1/subscriptions/active/{userId}", userId)
            .retrieve()
            .body(ActiveSubscriptionResponse.class);

        if (response == null || !response.active()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Active subscription required to start playback");
        }
    }

    public record ActiveSubscriptionResponse(UUID userId, boolean active, Object subscription) {
    }
}
