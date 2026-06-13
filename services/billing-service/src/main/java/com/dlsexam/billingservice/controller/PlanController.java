package com.dlsexam.billingservice.controller;

import com.dlsexam.billingservice.dto.BillingDtos.PlanResponse;
import com.dlsexam.billingservice.service.BillingService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final BillingService billingService;

    public PlanController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public List<PlanResponse> listPlans() {
        return billingService.listActivePlans();
    }

    @GetMapping("/{planId}")
    public PlanResponse getPlan(@PathVariable UUID planId) {
        return billingService.getPlan(planId);
    }
}
