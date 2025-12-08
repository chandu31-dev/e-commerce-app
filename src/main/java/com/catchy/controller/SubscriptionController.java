package com.catchy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.model.Subscription;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SubscriptionService subscriptionService;

    public static class CreateReq { public String planId; }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateReq req) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Please login"));
            Subscription s = subscriptionService.createLocalSubscription(user, req.planId, "active", 0);
            return ResponseEntity.ok(Map.of("success", true, "subscription", s));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody Map<String, String> body) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Please login"));
            String subIdStr = body.get("subscriptionId");
            if (subIdStr == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "subscriptionId required"));
            Subscription s = subscriptionService.findById(Long.parseLong(subIdStr));
            if (s == null) return ResponseEntity.status(404).body(Map.of("success", false, "message", "subscription not found"));
            if (!s.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).body(Map.of("success", false, "message", "forbidden"));
            subscriptionService.markCancelled(s);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> listUserSubs() {
        User user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Please login"));
        return ResponseEntity.ok(subscriptionService.getSubscriptionsForUser(user));
    }
}
