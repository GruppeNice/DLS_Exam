package com.dlsexam.billingservice.service;

import com.dlsexam.billingservice.domain.Invoice;
import com.dlsexam.billingservice.domain.InvoiceStatus;
import com.dlsexam.billingservice.domain.PaymentStatus;
import com.dlsexam.billingservice.domain.PaymentTransaction;
import com.dlsexam.billingservice.domain.Subscription;
import com.dlsexam.billingservice.domain.SubscriptionPlan;
import com.dlsexam.billingservice.domain.SubscriptionStatus;
import com.dlsexam.billingservice.dto.BillingDtos.ActivateSubscriptionRequest;
import com.dlsexam.billingservice.dto.BillingDtos.ActiveSubscriptionResponse;
import com.dlsexam.billingservice.dto.BillingDtos.InvoiceResponse;
import com.dlsexam.billingservice.dto.BillingDtos.PaymentResponse;
import com.dlsexam.billingservice.dto.BillingDtos.PlanResponse;
import com.dlsexam.billingservice.dto.BillingDtos.ProcessPaymentRequest;
import com.dlsexam.billingservice.dto.BillingDtos.RefundResponse;
import com.dlsexam.billingservice.dto.BillingDtos.SubscriptionResponse;
import com.dlsexam.billingservice.messaging.BillingEventPublisher;
import com.dlsexam.billingservice.repository.InvoiceRepository;
import com.dlsexam.billingservice.repository.PaymentTransactionRepository;
import com.dlsexam.billingservice.repository.SubscriptionPlanRepository;
import com.dlsexam.billingservice.repository.SubscriptionRepository;
import com.dlsexam.billingservice.service.PaymentGatewaySimulator.GatewayResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentGatewaySimulator paymentGateway;
    private final BillingEventPublisher eventPublisher;

    public BillingService(
        SubscriptionPlanRepository planRepository,
        SubscriptionRepository subscriptionRepository,
        PaymentTransactionRepository paymentRepository,
        InvoiceRepository invoiceRepository,
        PaymentGatewaySimulator paymentGateway,
        BillingEventPublisher eventPublisher
    ) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    public List<PlanResponse> listActivePlans() {
        return planRepository.findByActiveTrueOrderByPriceCentsAsc().stream()
            .map(this::toPlanResponse)
            .toList();
    }

    public PlanResponse getPlan(UUID planId) {
        SubscriptionPlan plan = findActivePlan(planId);
        return toPlanResponse(plan);
    }

    public List<SubscriptionResponse> listUserSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toSubscriptionResponse)
            .toList();
    }

    public ActiveSubscriptionResponse getActiveSubscription(UUID userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .map(subscription -> new ActiveSubscriptionResponse(userId, true, toSubscriptionResponse(subscription)))
            .orElseGet(() -> new ActiveSubscriptionResponse(userId, false, null));
    }

    @Transactional
    public SubscriptionResponse activateSubscription(UUID userId, ActivateSubscriptionRequest request, String idempotencyKey) {
        SubscriptionPlan plan = findActivePlan(request.planId());
        if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has an active subscription");
        }

        PaymentTransaction payment = resolveOrCreatePayment(userId, plan, idempotencyKey);
        if (payment.getStatus() == PaymentStatus.SUCCEEDED && payment.getSubscription() != null) {
            return toSubscriptionResponse(payment.getSubscription());
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, payment.getFailureReason());
        }

        GatewayResult gatewayResult = paymentGateway.charge(plan.getPriceCents(), plan.getCurrency(), userId);
        if (!gatewayResult.success()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(gatewayResult.failureReason());
            paymentRepository.save(payment);
            eventPublisher.paymentFailed(payment.getId(), userId, gatewayResult.failureReason(), Instant.now());
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, gatewayResult.failureReason());
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setGatewayReference(gatewayResult.reference());
        paymentRepository.save(payment);
        eventPublisher.paymentSucceeded(
            payment.getId(),
            userId,
            payment.getAmountCents(),
            payment.getCurrency(),
            Instant.now()
        );

        Subscription subscription = createSubscription(userId, plan);
        payment.setSubscription(subscription);
        paymentRepository.save(payment);

        Invoice invoice = createInvoice(userId, subscription, payment);
        invoiceRepository.save(invoice);

        eventPublisher.subscriptionActivated(
            subscription.getId(),
            userId,
            plan.getId(),
            plan.getCode(),
            Instant.now()
        );

        return toSubscriptionResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(UUID userId, UUID subscriptionId, String reason) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
        if (!subscription.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription does not belong to user");
        }
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subscription is not active");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(Instant.now());
        Subscription saved = subscriptionRepository.save(subscription);
        eventPublisher.subscriptionCancelled(
            saved.getId(),
            userId,
            saved.getPlan().getId(),
            reason == null ? "user_requested" : reason,
            Instant.now()
        );
        return toSubscriptionResponse(saved);
    }

    @Transactional
    public PaymentResponse processPayment(UUID userId, ProcessPaymentRequest request) {
        SubscriptionPlan plan = findActivePlan(request.planId());
        PaymentTransaction payment = resolveOrCreatePayment(userId, plan, request.idempotencyKey());
        if (payment.getStatus() == PaymentStatus.PENDING) {
            GatewayResult gatewayResult = paymentGateway.charge(plan.getPriceCents(), plan.getCurrency(), userId);
            if (gatewayResult.success()) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setGatewayReference(gatewayResult.reference());
                eventPublisher.paymentSucceeded(
                    payment.getId(),
                    userId,
                    payment.getAmountCents(),
                    payment.getCurrency(),
                    Instant.now()
                );
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(gatewayResult.failureReason());
                eventPublisher.paymentFailed(payment.getId(), userId, gatewayResult.failureReason(), Instant.now());
            }
            paymentRepository.save(payment);
        }
        return toPaymentResponse(payment);
    }

    @Transactional
    public RefundResponse refundPayment(UUID userId, UUID paymentId) {
        PaymentTransaction payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (!payment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment does not belong to user");
        }
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only succeeded payments can be refunded");
        }

        GatewayResult gatewayResult = paymentGateway.refund(payment.getGatewayReference());
        if (!gatewayResult.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, gatewayResult.failureReason());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        if (payment.getSubscription() != null && payment.getSubscription().getStatus() == SubscriptionStatus.ACTIVE) {
            Subscription subscription = payment.getSubscription();
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setCancelledAt(Instant.now());
            subscriptionRepository.save(subscription);
            eventPublisher.subscriptionCancelled(
                subscription.getId(),
                userId,
                subscription.getPlan().getId(),
                "refund",
                Instant.now()
            );
        }

        return new RefundResponse(payment.getId(), payment.getStatus(), gatewayResult.reference());
    }

    public List<InvoiceResponse> listUserInvoices(UUID userId) {
        return invoiceRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
            .map(this::toInvoiceResponse)
            .toList();
    }

    private PaymentTransaction resolveOrCreatePayment(UUID userId, SubscriptionPlan plan, String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
            .map(existing -> {
                if (!existing.getUserId().equals(userId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency key already used by another user");
                }
                return existing;
            })
            .orElseGet(() -> {
                PaymentTransaction payment = new PaymentTransaction();
                payment.setUserId(userId);
                payment.setPlan(plan);
                payment.setAmountCents(plan.getPriceCents());
                payment.setCurrency(plan.getCurrency());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setIdempotencyKey(idempotencyKey);
                return paymentRepository.save(payment);
            });
    }

    private Subscription createSubscription(UUID userId, SubscriptionPlan plan) {
        Instant now = Instant.now();
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(now);
        subscription.setEndsAt(now.plus(plan.getBillingPeriodDays(), ChronoUnit.DAYS));
        return subscriptionRepository.save(subscription);
    }

    private Invoice createInvoice(UUID userId, Subscription subscription, PaymentTransaction payment) {
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setPaymentTransaction(payment);
        invoice.setSubscription(subscription);
        invoice.setAmountCents(payment.getAmountCents());
        invoice.setCurrency(payment.getCurrency());
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(Instant.now());
        return invoice;
    }

    private SubscriptionPlan findActivePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (!plan.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan is not active");
        }
        return plan;
    }

    private PlanResponse toPlanResponse(SubscriptionPlan plan) {
        return new PlanResponse(
            plan.getId(),
            plan.getCode(),
            plan.getName(),
            plan.getDescription(),
            plan.getPriceCents(),
            plan.getCurrency(),
            plan.getBillingPeriodDays(),
            plan.isActive()
        );
    }

    private SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        SubscriptionPlan plan = subscription.getPlan();
        return new SubscriptionResponse(
            subscription.getId(),
            subscription.getUserId(),
            plan.getId(),
            plan.getCode(),
            plan.getName(),
            subscription.getStatus(),
            subscription.getStartedAt(),
            subscription.getEndsAt(),
            subscription.getCancelledAt()
        );
    }

    private PaymentResponse toPaymentResponse(PaymentTransaction payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getUserId(),
            payment.getSubscription() == null ? null : payment.getSubscription().getId(),
            payment.getPlan() == null ? null : payment.getPlan().getId(),
            payment.getAmountCents(),
            payment.getCurrency(),
            payment.getStatus(),
            payment.getGatewayReference(),
            payment.getFailureReason(),
            payment.getCreatedAt()
        );
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
            invoice.getId(),
            invoice.getUserId(),
            invoice.getPaymentTransaction().getId(),
            invoice.getSubscription() == null ? null : invoice.getSubscription().getId(),
            invoice.getAmountCents(),
            invoice.getCurrency(),
            invoice.getStatus(),
            invoice.getIssuedAt()
        );
    }
}
