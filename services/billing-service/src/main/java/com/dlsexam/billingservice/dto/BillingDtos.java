package com.dlsexam.billingservice.dto;

import com.dlsexam.billingservice.domain.InvoiceStatus;
import com.dlsexam.billingservice.domain.PaymentStatus;
import com.dlsexam.billingservice.domain.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class BillingDtos {

    private BillingDtos() {
    }

    public record PlanResponse(
        UUID id,
        String code,
        String name,
        String description,
        long priceCents,
        String currency,
        int billingPeriodDays,
        boolean active
    ) {
    }

    public record ActivateSubscriptionRequest(
        @NotNull UUID planId
    ) {
    }

    public record SubscriptionResponse(
        UUID id,
        UUID userId,
        UUID planId,
        String planCode,
        String planName,
        SubscriptionStatus status,
        Instant startedAt,
        Instant endsAt,
        Instant cancelledAt
    ) {
    }

    public record ActiveSubscriptionResponse(
        UUID userId,
        boolean active,
        SubscriptionResponse subscription
    ) {
    }

    public record PaymentResponse(
        UUID id,
        UUID userId,
        UUID subscriptionId,
        UUID planId,
        long amountCents,
        String currency,
        PaymentStatus status,
        String gatewayReference,
        String failureReason,
        Instant createdAt
    ) {
    }

    public record RefundResponse(
        UUID paymentId,
        PaymentStatus status,
        String gatewayReference
    ) {
    }

    public record InvoiceResponse(
        UUID id,
        UUID userId,
        UUID paymentTransactionId,
        UUID subscriptionId,
        long amountCents,
        String currency,
        InvoiceStatus status,
        Instant issuedAt
    ) {
    }

    public record ProcessPaymentRequest(
        @NotNull UUID planId,
        @NotBlank String idempotencyKey
    ) {
    }
}
