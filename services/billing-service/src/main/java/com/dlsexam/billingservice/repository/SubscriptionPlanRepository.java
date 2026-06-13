package com.dlsexam.billingservice.repository;

import com.dlsexam.billingservice.domain.SubscriptionPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    List<SubscriptionPlan> findByActiveTrueOrderByPriceCentsAsc();

    Optional<SubscriptionPlan> findByCodeIgnoreCase(String code);
}
