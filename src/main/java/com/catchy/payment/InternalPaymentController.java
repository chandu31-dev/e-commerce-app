package com.catchy.payment;

import com.catchy.payment.dto.PaymentRequest;
import com.catchy.payment.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/internal-payments")
public class InternalPaymentController {

    @Autowired
    private com.catchy.payment.InternalPaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiate(@RequestBody PaymentRequest req) {
        PaymentResponse resp = paymentService.createPayment(req);
        return ResponseEntity.created(URI.create("/api/internal-payments/" + resp.getPaymentId())).body(resp);
    }

    @PostMapping("/verify/{paymentId}")
    public ResponseEntity<PaymentResponse> verify(@PathVariable Long paymentId, @RequestParam String providerTxnId) {
        PaymentResponse resp = paymentService.verifyPayment(paymentId, providerTxnId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentLedger> get(@PathVariable Long id) {
        PaymentLedger p = paymentService.find(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }
}
