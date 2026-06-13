package com.dlsexam.billingservice.repository;

import com.dlsexam.billingservice.domain.Subscription;
import com.dlsexam.billingservice.domain.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}
