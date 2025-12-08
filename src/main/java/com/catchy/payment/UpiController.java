package com.catchy.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/internal-payments")
public class UpiController {

    @Autowired
    private InternalPaymentService internalPaymentService;

    @GetMapping(value = "/{id}/upi", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUpi(@PathVariable("id") Long id) {
        String upi = internalPaymentService.generateUpiPayload(id);
        if (upi == null) {
            return ResponseEntity.notFound().build();
        }

        // Extract UPI ID (VPA) from the deep-link
        String upiId = extractUpiId(upi);

        Map<String, String> resp = new HashMap<>();
        resp.put("upiLink", upi);
        resp.put("upiId", upiId);
        return ResponseEntity.ok(resp);
    }

    private String extractUpiId(String upiLink) {
        // Extract the VPA (upiId) from the deep-link
        // Format: upi://pay?pa=chandukiran2513@ybl&...
        if (upiLink != null && upiLink.contains("pa=")) {
            String[] parts = upiLink.split("pa=");
            if (parts.length > 1) {
                String vpa = parts[1].split("&")[0];
                return vpa;
            }
        }
        return "";
    }
}
