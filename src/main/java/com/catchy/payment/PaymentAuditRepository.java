package com.catchy.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, Long> {
    List<PaymentAudit> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}
