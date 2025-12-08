package com.catchy.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Payout;
import com.catchy.model.Vendor;
import com.catchy.repository.PayoutRepository;

@Service
public class PayoutService {

    private final PayoutRepository payoutRepository;

    public PayoutService(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Transactional
    public Payout createPayout(Vendor vendor, BigDecimal amount, BigDecimal commission) {
        Payout p = new Payout(vendor, amount, commission);
        return payoutRepository.save(p);
    }

    public List<Payout> getPayoutsForVendor(Long vendorId) {
        return payoutRepository.findByVendorId(vendorId);
    }

    public List<Payout> getPayoutsForVendorByStatus(Long vendorId, Payout.Status status) {
        return payoutRepository.findByVendorIdAndStatus(vendorId, status);
    }

    public List<Payout> findPendingOlderThanDays(int days) {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(days);
        return payoutRepository.findByStatusAndCreatedAtBefore(Payout.Status.PENDING, cutoff);
    }

    @Transactional
    public void markPayoutsAsPaidBatch(List<Payout> payouts, String batchReference) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Payout p : payouts) {
            p.setStatus(Payout.Status.PAID);
            p.setPaidAt(now);
            p.setReference(batchReference);
            payoutRepository.save(p);
        }
    }

    public Optional<Payout> findById(Long id) {
        return payoutRepository.findById(id);
    }

    @Transactional
    public Payout markAsPaid(Long payoutId, String reference) {
        Payout p = payoutRepository.findById(payoutId).orElseThrow();
        p.setStatus(Payout.Status.PAID);
        p.setPaidAt(LocalDateTime.now());
        p.setReference(reference);
        return payoutRepository.save(p);
    }
}
