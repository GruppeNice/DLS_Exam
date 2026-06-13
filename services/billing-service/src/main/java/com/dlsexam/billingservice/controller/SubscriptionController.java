package com.dlsexam.billingservice.controller;

import com.dlsexam.billingservice.dto.BillingDtos.ActivateSubscriptionRequest;
import com.dlsexam.billingservice.dto.BillingDtos.ActiveSubscriptionResponse;
import com.dlsexam.billingservice.dto.BillingDtos.SubscriptionResponse;
import com.dlsexam.billingservice.security.UserPrincipal;
import com.dlsexam.billingservice.service.BillingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final BillingService billingService;

    public SubscriptionController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/me")
    public List<SubscriptionResponse> mySubscriptions(@AuthenticationPrincipal UserPrincipal principal) {
        return billingService.listUserSubscriptions(principal.getId());
    }

    @GetMapping("/active/{userId}")
    public ActiveSubscriptionResponse activeSubscription(@PathVariable UUID userId) {
        return billingService.getActiveSubscription(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse activate(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ActivateSubscriptionRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        String key = requireIdempotencyKey(idempotencyKey);
        return billingService.activateSubscription(principal.getId(), request, key);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public SubscriptionResponse cancel(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID subscriptionId,
        @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body == null ? null : body.get("reason");
        return billingService.cancelSubscription(principal.getId(), subscriptionId, reason);
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        return idempotencyKey.trim();
    }
}
