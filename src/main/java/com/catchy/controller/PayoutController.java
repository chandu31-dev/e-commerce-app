package com.catchy.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.model.Payout;
import com.catchy.model.Vendor;
import com.catchy.repository.VendorRepository;
import com.catchy.service.PayoutService;

@RestController
@RequestMapping("/api/admin/vendors")
public class PayoutController {

    private final PayoutService payoutService;
    private final VendorRepository vendorRepository;

    public static class CreatePayoutRequest {
        public BigDecimal amount;
        public BigDecimal commission;
    }

    public PayoutController(PayoutService payoutService, VendorRepository vendorRepository) {
        this.payoutService = payoutService;
        this.vendorRepository = vendorRepository;
    }

    @PostMapping("/{vendorId}/payouts")
    public ResponseEntity<?> createPayout(@PathVariable Long vendorId, @RequestBody CreatePayoutRequest req) {
        Vendor vendor = vendorRepository.findById(vendorId).orElseThrow();
        Payout p = payoutService.createPayout(vendor, req.amount, req.commission != null ? req.commission : BigDecimal.ZERO);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{vendorId}/payouts")
    public ResponseEntity<List<Payout>> listPayouts(@PathVariable Long vendorId) {
        return ResponseEntity.ok(payoutService.getPayoutsForVendor(vendorId));
    }

    @GetMapping("/{vendorId}/payouts/export")
    public ResponseEntity<?> exportPayoutsCsv(@PathVariable Long vendorId, @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "ALL") String status) {
        java.util.List<Payout> payouts;
        if ("ALL".equalsIgnoreCase(status)) {
            payouts = payoutService.getPayoutsForVendor(vendorId);
        } else {
            Payout.Status s = Payout.Status.valueOf(status.toUpperCase());
            payouts = payoutService.getPayoutsForVendorByStatus(vendorId, s);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("id,vendorId,amount,commission,status,createdAt,paidAt,reference\n");
        for (Payout p : payouts) {
            sb.append(p.getId()).append(",")
              .append(p.getVendor().getId()).append(",")
              .append(p.getAmount()).append(",")
              .append(p.getCommission()).append(",")
              .append(p.getStatus()).append(",")
              .append(p.getCreatedAt()).append(",")
              .append(p.getPaidAt() != null ? p.getPaidAt() : "").append(",")
              .append(p.getReference() != null ? p.getReference().replace(',', '-') : "")
              .append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=vendor-" + vendorId + "-payouts.csv")
                .header("Content-Type", "text/csv")
                .body(sb.toString());
    }
}
