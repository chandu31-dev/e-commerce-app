package com.catchy.payment;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catchy.model.Order;

@Service
public class PayUPaymentService {

    @Autowired
    private PayUConfig payUConfig;

    /**
     * Create a PayU payment request
     */
    public Map<String, String> createPaymentRequest(Order order, String returnUrl) throws Exception {
        Map<String, String> params = new HashMap<>();
        String txnId = "TXN" + System.currentTimeMillis();

        params.put("key", payUConfig.getMerchantKey());
        params.put("txnid", txnId);
        params.put("amount", String.format("%.2f", order.getTotalPrice()));
        params.put("productinfo", "Order #" + order.getId());
        params.put("firstname", order.getUser().getName());
        params.put("email", order.getUser().getEmail());
        params.put("phone", "9999999999"); // default phone
        params.put("surl", returnUrl);
        params.put("furl", returnUrl);
        params.put("curl", returnUrl);

        // Calculate hash
        String hashString = String.format("%s|%s|%s|%s|%s|%s|||||||||%s",
                payUConfig.getMerchantKey(),
                txnId,
                order.getTotalPrice(),
                "Order #" + order.getId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                payUConfig.getMerchantSalt());

        params.put("hash", generateHash(hashString));
        params.put("action", payUConfig.getPaymentUrl());

        return params;
    }

    /**
     * Generate SHA512 hash for PayU
     */
    private String generateHash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] messageDigest = md.digest(input.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verify PayU response
     */
    public boolean verifyPaymentResponse(String txnId, String amount, String status, String hash)
            throws Exception {
        String hashString = String.format("%s|%s|%s|%s",
                payUConfig.getMerchantSalt(),
                status,
                txnId,
                amount);

        String calculatedHash = generateHash(hashString);
        return calculatedHash.equalsIgnoreCase(hash);
    }
}
