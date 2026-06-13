package com.dlsexam.billingservice.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewaySimulator {

    private final double simulatedFailureRate;

    public PaymentGatewaySimulator(
        @Value("${app.payment-gateway.simulated-failure-rate}") double simulatedFailureRate
    ) {
        this.simulatedFailureRate = simulatedFailureRate;
    }

    public GatewayResult charge(long amountCents, String currency, UUID userId) {
        if (amountCents <= 0) {
            return GatewayResult.failure("INVALID_AMOUNT", "Amount must be positive");
        }
        if (Math.random() < simulatedFailureRate) {
            return GatewayResult.failure("GATEWAY_DECLINED", "Simulated payment gateway decline");
        }
        String reference = "gw_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return GatewayResult.success(reference);
    }

    public GatewayResult refund(String gatewayReference) {
        if (gatewayReference == null || gatewayReference.isBlank()) {
            return GatewayResult.failure("INVALID_REFERENCE", "Missing gateway reference");
        }
        return GatewayResult.success("refund_" + gatewayReference);
    }

    public record GatewayResult(boolean success, String reference, String failureCode, String failureReason) {

        public static GatewayResult success(String reference) {
            return new GatewayResult(true, reference, null, null);
        }

        public static GatewayResult failure(String code, String reason) {
            return new GatewayResult(false, null, code, reason);
        }
    }
}
