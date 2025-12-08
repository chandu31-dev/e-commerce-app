package com.catchy.payment;

import com.catchy.payment.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private InternalPaymentRepository paymentRepository;

    @Autowired
    private com.catchy.payment.InternalPaymentService paymentService;

    @Autowired
    private PaymentAuditRepository paymentAuditRepository;

    @GetMapping
    public ResponseEntity<List<PaymentLedger>> list(@RequestParam(required = false) String status) {
        List<PaymentLedger> all = paymentRepository.findAll();
        if (status == null) return ResponseEntity.ok(all);
        List<PaymentLedger> filtered = all.stream().filter(p -> p.getStatus() != null && p.getStatus().name().equalsIgnoreCase(status)).collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/manual-settle/{id}")
    public ResponseEntity<PaymentResponse> manualSettle(@PathVariable Long id, @RequestParam(required = false) String txn, @RequestParam(required = false) String actor) {
        PaymentResponse resp = paymentService.manualSettle(id, txn == null ? "MANUAL-" + id : txn, actor);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/mark-reconciled/{id}")
    public ResponseEntity<String> markReconciled(@PathVariable Long id, @RequestParam(required = false) String actor) {
        PaymentLedger p = paymentRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        p.setStatus(PaymentStatus.RECONCILED);
        paymentRepository.save(p);

        PaymentAudit audit = new PaymentAudit();
        audit.setPaymentId(p.getId());
        audit.setAction("MARK_RECONCILED");
        audit.setActor(actor == null ? "system" : actor);
        audit.setNotes("Marked reconciled by " + (actor == null ? "system" : actor));
        paymentAuditRepository.save(audit);

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/audits/{paymentId}")
    public ResponseEntity<List<PaymentAudit>> audits(@PathVariable Long paymentId) {
        List<PaymentAudit> list = paymentAuditRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId);
        return ResponseEntity.ok(list);
    }
}
