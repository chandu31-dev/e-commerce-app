package com.catchy.payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternalPaymentRepository extends JpaRepository<PaymentLedger, Long> {
    List<PaymentLedger> findByOrderId(Long orderId);
}
