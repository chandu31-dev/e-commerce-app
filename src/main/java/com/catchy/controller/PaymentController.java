package com.catchy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-intent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createIntent(@RequestParam(required = false) Long amountCents) {
        if (amountCents == null || amountCents <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid amount"));
        }
        Map<String, Object> resp = paymentService.createIntentForOrder(amountCents);
        return ResponseEntity.ok(resp);
    }
}
