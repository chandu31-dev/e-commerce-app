package com.catchy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.catchy.model.Payout;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findByVendorId(Long vendorId);
    List<Payout> findByStatusAndCreatedAtBefore(Payout.Status status, java.time.LocalDateTime before);
    List<Payout> findByVendorIdAndStatus(Long vendorId, Payout.Status status);
}
