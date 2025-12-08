package com.catchy.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.dto.ApiResponse;
import com.catchy.model.Order;
import com.catchy.payment.PayUPaymentService;
import com.catchy.repository.OrderRepository;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PayUPaymentService payUPaymentService;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Initiate PayU payment for an order
     */
    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> initiatePayment(@PathVariable Long orderId)
            throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String returnUrl = "http://localhost:8080/api/payment/verify";
        Map<String, String> paymentParams = payUPaymentService.createPaymentRequest(order, returnUrl);

        return ResponseEntity.ok(new ApiResponse<>(true, "Payment request created", paymentParams));
    }

    /**
     * Handle PayU payment response (success/failure)
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @RequestParam("txnid") String txnId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("status") String status,
            @RequestParam("hash") String hash) throws Exception {

        if ("success".equalsIgnoreCase(status)) {
            boolean isValid = payUPaymentService.verifyPaymentResponse(txnId, amount.toString(), status, hash);
            if (isValid) {
                // Mark order as paid
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment verified successfully", "OK"));
            }
        }

        return ResponseEntity.status(400)
                .body(new ApiResponse<>(false, "Payment verification failed", null));
    }

    /**
     * Get payment status for an order
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<String>> getPaymentStatus(@PathVariable Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return ResponseEntity.ok(new ApiResponse<>(true, "Payment status retrieved", "PENDING"));
    }
}
