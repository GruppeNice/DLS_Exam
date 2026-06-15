package com.dlsexam.billingservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dlsexam.billingservice.domain.Invoice;
import com.dlsexam.billingservice.domain.InvoiceStatus;
import com.dlsexam.billingservice.domain.PaymentStatus;
import com.dlsexam.billingservice.domain.PaymentTransaction;
import com.dlsexam.billingservice.domain.Subscription;
import com.dlsexam.billingservice.domain.SubscriptionPlan;
import com.dlsexam.billingservice.domain.SubscriptionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=CHAR"
})
class BillingRepositoryIntegrationTest {

    @Autowired
    private SubscriptionPlanRepository planRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    void paymentTransactionRepository_findsByIdempotencyKey() {
        SubscriptionPlan plan = savePlan("PLAN_A");

        PaymentTransaction payment = new PaymentTransaction();
        payment.setUserId(UUID.randomUUID());
        payment.setPlan(plan);
        payment.setAmountCents(1500L);
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey("idem-test-1");
        paymentTransactionRepository.save(payment);

        assertThat(paymentTransactionRepository.findByIdempotencyKey("idem-test-1"))
            .isPresent()
            .get()
            .extracting(PaymentTransaction::getAmountCents)
            .isEqualTo(1500L);
    }

    @Test
    void subscriptionRepository_filtersByUserAndActiveStatus() {
        SubscriptionPlan plan = savePlan("PLAN_B");
        UUID userId = UUID.randomUUID();

        Subscription active = new Subscription();
        active.setUserId(userId);
        active.setPlan(plan);
        active.setStatus(SubscriptionStatus.ACTIVE);
        active.setStartedAt(Instant.now());
        active.setEndsAt(Instant.now().plusSeconds(86400));
        subscriptionRepository.save(active);

        Subscription cancelled = new Subscription();
        cancelled.setUserId(userId);
        cancelled.setPlan(plan);
        cancelled.setStatus(SubscriptionStatus.CANCELLED);
        cancelled.setStartedAt(Instant.now().minusSeconds(86400));
        cancelled.setEndsAt(Instant.now());
        cancelled.setCancelledAt(Instant.now());
        subscriptionRepository.save(cancelled);

        assertThat(subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).isTrue();
        assertThat(subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).isPresent();
    }

    @Test
    void invoiceRepository_returnsInvoicesOrderedByIssuedAtDesc() {
        SubscriptionPlan plan = savePlan("PLAN_C");
        UUID userId = UUID.randomUUID();

        PaymentTransaction paymentA = new PaymentTransaction();
        paymentA.setUserId(userId);
        paymentA.setPlan(plan);
        paymentA.setAmountCents(1000L);
        paymentA.setCurrency("USD");
        paymentA.setStatus(PaymentStatus.SUCCEEDED);
        paymentA.setIdempotencyKey("idem-inv-a");
        paymentTransactionRepository.save(paymentA);

        PaymentTransaction paymentB = new PaymentTransaction();
        paymentB.setUserId(userId);
        paymentB.setPlan(plan);
        paymentB.setAmountCents(2000L);
        paymentB.setCurrency("USD");
        paymentB.setStatus(PaymentStatus.SUCCEEDED);
        paymentB.setIdempotencyKey("idem-inv-b");
        paymentTransactionRepository.save(paymentB);

        Invoice older = new Invoice();
        older.setUserId(userId);
        older.setPaymentTransaction(paymentA);
        older.setAmountCents(1000L);
        older.setCurrency("USD");
        older.setStatus(InvoiceStatus.ISSUED);
        older.setIssuedAt(Instant.parse("2025-01-01T00:00:00Z"));
        invoiceRepository.save(older);

        Invoice newer = new Invoice();
        newer.setUserId(userId);
        newer.setPaymentTransaction(paymentB);
        newer.setAmountCents(2000L);
        newer.setCurrency("USD");
        newer.setStatus(InvoiceStatus.ISSUED);
        newer.setIssuedAt(Instant.parse("2025-01-02T00:00:00Z"));
        invoiceRepository.save(newer);

        List<Invoice> invoices = invoiceRepository.findByUserIdOrderByIssuedAtDesc(userId);

        assertThat(invoices).hasSize(2);
        assertThat(invoices.get(0).getIssuedAt()).isAfter(invoices.get(1).getIssuedAt());
    }

    private SubscriptionPlan savePlan(String code) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setCode(code);
        plan.setName("Plan " + code);
        plan.setDescription("Integration test plan");
        plan.setPriceCents(999L);
        plan.setCurrency("USD");
        plan.setBillingPeriodDays(30);
        plan.setActive(true);
        return planRepository.save(plan);
    }
}

