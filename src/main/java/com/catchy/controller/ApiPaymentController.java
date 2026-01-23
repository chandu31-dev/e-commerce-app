package com.catchy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.catchy.dto.PaymentIntentRequest;
import com.catchy.dto.PaymentResponse;
import com.catchy.model.Order;
import com.catchy.service.OrderService;
import com.catchy.service.PaymentService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class ApiPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createIntent(@RequestBody PaymentIntentRequest req) {
        try {
            if (req == null || req.getOrderId() == null) {
                return ResponseEntity.badRequest().body(new PaymentResponse(false, "Missing orderId"));
            }

            Order order = orderService.getOrderById(req.getOrderId());
            if (order == null) return ResponseEntity.badRequest().body(new PaymentResponse(false, "Order not found"));

            BigDecimal amount = req.getAmountInr() == null ? order.getTotalPrice() : req.getAmountInr();
            PaymentResponse resp = paymentService.createPaymentIntentForOrder(order, amount == null ? BigDecimal.ZERO : amount);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new PaymentResponse(false, "Internal error: " + ex.getMessage()));
        }
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> confirm(@PathVariable String paymentIntentId) {
        try {
            if (paymentIntentId == null || paymentIntentId.isBlank()) {
                return ResponseEntity.badRequest().body(new PaymentResponse(false, "Missing paymentIntentId"));
            }

            PaymentResponse resp = paymentService.confirmPaymentIntent(paymentIntentId);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new PaymentResponse(false, "Internal error: " + ex.getMessage()));
        }
    }
}
