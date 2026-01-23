package com.catchy.payment;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.payment.dto.PaymentRequest;
import com.catchy.payment.dto.PaymentResponse;

@RestController
@RequestMapping("/api/internal-payments")
public class InternalPaymentController {

    @Autowired
    private com.catchy.payment.InternalPaymentService paymentService;

    @Autowired
    private com.catchy.service.AuthService authService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiate(@RequestBody PaymentRequest req) {
        try {
            var user = authService.getCurrentUser();
            if (user != null && (req.getUserId() == null || req.getUserId() == 0L)) {
                req.setUserId(user.getId());
            }
        } catch (Exception ignored) {}
        PaymentResponse resp = paymentService.createPayment(req);
        return ResponseEntity.created(URI.create("/api/internal-payments/" + resp.getPaymentId())).body(resp);
    }

    @PostMapping("/verify/{paymentId}")
    public ResponseEntity<PaymentResponse> verify(@PathVariable Long paymentId, @RequestParam String providerTxnId) {
        PaymentResponse resp = paymentService.verifyPayment(paymentId, providerTxnId);
        // if payment succeeded, attempt to complete order
        if (resp.getStatus() != null && resp.getStatus().name().equals("SUCCESS")) {
            try {
                paymentService.completeOrderIfPaymentSuccess(paymentId);
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/method")
    public ResponseEntity<PaymentResponse> setMethod(@PathVariable Long id, @RequestParam("method") String method) {
        com.catchy.payment.PaymentMethod pm = null;
        try { pm = com.catchy.payment.PaymentMethod.valueOf(method); } catch (Exception ignored) {}
        PaymentResponse resp = paymentService.initiateMethod(id, pm);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/cod")
    public ResponseEntity<PaymentResponse> codPay(@PathVariable Long id) {
        PaymentResponse resp = paymentService.settleCod(id, "cod");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {
        try {
            paymentService.completeOrderIfPaymentSuccess(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentLedger> get(@PathVariable Long id) {
        PaymentLedger p = paymentService.find(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // Dev helper: mock a successful payment for testing (marks payment SUCCESS)
    @PostMapping("/{id}/mock-success")
    public ResponseEntity<PaymentResponse> mockSuccess(@PathVariable Long id) {
        PaymentResponse resp = paymentService.manualSettle(id, "MOCK-SUCCESS-1", "dev");
        try { paymentService.completeOrderIfPaymentSuccess(id); } catch (Exception ignored) {}
        return ResponseEntity.ok(resp);
    }
}
