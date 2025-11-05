package com.catchy.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class PaymentService {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    public PaymentIntent createPaymentIntent(long amountCents, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency)
                .build();

        return PaymentIntent.create(params);
    }

    public Map<String, Object> createIntentForOrder(long amountCents) {
        Map<String, Object> map = new HashMap<>();
        try {
            PaymentIntent pi = createPaymentIntent(amountCents, "usd");
            map.put("clientSecret", pi.getClientSecret());
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("message", e.getMessage());
        }
        return map;
    }
}
