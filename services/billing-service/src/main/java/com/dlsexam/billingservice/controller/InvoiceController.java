package com.dlsexam.billingservice.controller;

import com.dlsexam.billingservice.dto.BillingDtos.InvoiceResponse;
import com.dlsexam.billingservice.security.UserPrincipal;
import com.dlsexam.billingservice.service.BillingService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public List<InvoiceResponse> listInvoices(@AuthenticationPrincipal UserPrincipal principal) {
        return billingService.listUserInvoices(principal.getId());
    }
}
