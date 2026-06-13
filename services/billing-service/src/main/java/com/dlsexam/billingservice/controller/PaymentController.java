package com.dlsexam.billingservice.controller;

import com.dlsexam.billingservice.dto.BillingDtos.PaymentResponse;
import com.dlsexam.billingservice.dto.BillingDtos.ProcessPaymentRequest;
import com.dlsexam.billingservice.dto.BillingDtos.RefundResponse;
import com.dlsexam.billingservice.security.UserPrincipal;
import com.dlsexam.billingservice.service.BillingService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final BillingService billingService;

    public PaymentController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse processPayment(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ProcessPaymentRequest request
    ) {
        return billingService.processPayment(principal.getId(), request);
    }

    @PostMapping("/{paymentId}/refund")
    public RefundResponse refundPayment(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID paymentId
    ) {
        return billingService.refundPayment(principal.getId(), paymentId);
    }
}
