package com.dlsexam.billingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dlsexam.billingservice.domain.PaymentStatus;
import com.dlsexam.billingservice.domain.PaymentTransaction;
import com.dlsexam.billingservice.domain.Subscription;
import com.dlsexam.billingservice.domain.SubscriptionPlan;
import com.dlsexam.billingservice.domain.SubscriptionStatus;
import com.dlsexam.billingservice.dto.BillingDtos.ActivateSubscriptionRequest;
import com.dlsexam.billingservice.dto.BillingDtos.RefundResponse;
import com.dlsexam.billingservice.dto.BillingDtos.SubscriptionResponse;
import com.dlsexam.billingservice.messaging.BillingEventPublisher;
import com.dlsexam.billingservice.repository.InvoiceRepository;
import com.dlsexam.billingservice.repository.PaymentTransactionRepository;
import com.dlsexam.billingservice.repository.SubscriptionPlanRepository;
import com.dlsexam.billingservice.repository.SubscriptionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private SubscriptionPlanRepository planRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PaymentTransactionRepository paymentRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentGatewaySimulator paymentGateway;
    @Mock
    private BillingEventPublisher eventPublisher;

    @Test
    void activateSubscription_success_createsSubscriptionAndPublishesEvents() {
        BillingService service = buildService();
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        SubscriptionPlan plan = activePlan(planId);
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.charge(plan.getPriceCents(), plan.getCurrency(), userId))
            .thenReturn(PaymentGatewaySimulator.GatewayResult.success("gw-ref-1"));

        SubscriptionResponse response = service.activateSubscription(
            userId,
            new ActivateSubscriptionRequest(planId),
            "idem-1"
        );

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.planId()).isEqualTo(planId);
        assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        verify(paymentRepository, atLeastOnce()).save(any(PaymentTransaction.class));
        verify(invoiceRepository).save(any());
        verify(eventPublisher).paymentSucceeded(any(), any(), anyLong(), anyString(), any(Instant.class));
        verify(eventPublisher).subscriptionActivated(any(), any(), any(), anyString(), any(Instant.class));
    }

    @Test
    void activateSubscription_gatewayFailure_marksPaymentFailedAndThrowsPaymentRequired() {
        BillingService service = buildService();
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        SubscriptionPlan plan = activePlan(planId);
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(paymentRepository.findByIdempotencyKey("idem-2")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.charge(plan.getPriceCents(), plan.getCurrency(), userId))
            .thenReturn(PaymentGatewaySimulator.GatewayResult.failure("DECLINED", "Card declined"));

        assertThatThrownBy(() -> service.activateSubscription(
            userId,
            new ActivateSubscriptionRequest(planId),
            "idem-2"
        ))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.PAYMENT_REQUIRED);

        verify(eventPublisher).paymentFailed(any(), any(), anyString(), any(Instant.class));
    }

    @Test
    void refundPayment_success_cancelsActiveSubscriptionAndReturnsRefund() {
        BillingService service = buildService();
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        SubscriptionPlan plan = activePlan(planId);
        ReflectionTestUtils.setField(plan, "id", planId);

        Subscription subscription = new Subscription();
        ReflectionTestUtils.setField(subscription, "id", subscriptionId);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        PaymentTransaction payment = new PaymentTransaction();
        ReflectionTestUtils.setField(payment, "id", paymentId);
        payment.setUserId(userId);
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setGatewayReference("gw-ref-2");
        payment.setSubscription(subscription);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentGateway.refund("gw-ref-2")).thenReturn(PaymentGatewaySimulator.GatewayResult.success("refund-gw-ref-2"));
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundResponse response = service.refundPayment(userId, paymentId);

        assertThat(response.paymentId()).isEqualTo(paymentId);
        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        verify(eventPublisher).subscriptionCancelled(any(), any(), any(), anyString(), any(Instant.class));
    }

    private BillingService buildService() {
        return new BillingService(
            planRepository,
            subscriptionRepository,
            paymentRepository,
            invoiceRepository,
            paymentGateway,
            eventPublisher
        );
    }

    private SubscriptionPlan activePlan(UUID id) {
        SubscriptionPlan plan = new SubscriptionPlan();
        ReflectionTestUtils.setField(plan, "id", id);
        plan.setCode("BASIC");
        plan.setName("Basic");
        plan.setDescription("Basic plan");
        plan.setPriceCents(999L);
        plan.setCurrency("USD");
        plan.setBillingPeriodDays(30);
        plan.setActive(true);
        return plan;
    }
}

