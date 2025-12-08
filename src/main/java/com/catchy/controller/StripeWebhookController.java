package com.catchy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.service.SubscriptionService;

@RestController
@RequestMapping("${stripe.webhook.path:/api/webhooks/stripe}")
public class StripeWebhookController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null) {
                Map<String, Object> obj = (Map<String, Object>) data.get("object");
                if (obj != null) {
                    String id = (String) obj.get("id");
                    if (id != null && type != null && type.contains("subscription")) {
                        if (type.contains("deleted") || type.contains("cancelled") || type.contains("canceled")) {
                            var s = subscriptionService.findByStripeId(id);
                            if (s != null) subscriptionService.markCancelled(s);
                        } else if (type.contains("created") || type.contains("updated")) {
                            // noop for now; real webhook should update status/period ends
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("received", true));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }
}

