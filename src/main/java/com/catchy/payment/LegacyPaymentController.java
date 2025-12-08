package com.catchy.payment;

import com.catchy.payment.dto.PaymentRequest;
import com.catchy.payment.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

 

@RestController
@RequestMapping("/api/legacy-payments")
@Profile("legacy-off")
public class LegacyPaymentController {

    @Autowired(required = false)
    private com.catchy.service.PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiate(@RequestBody PaymentRequest req) {
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/verify/{paymentId}")
    public ResponseEntity<PaymentResponse> verify(@PathVariable Long paymentId, @RequestParam String providerTxnId) {
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> get(@PathVariable Long id) {
        return ResponseEntity.status(501).build();
    }
}
