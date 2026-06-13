package com.dlsexam.billingservice.repository;

import com.dlsexam.billingservice.domain.Invoice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByUserIdOrderByIssuedAtDesc(UUID userId);
}
