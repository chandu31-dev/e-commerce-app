package com.catchy.stripe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.catchy.model.User;

/**
 * Lightweight Stripe subscription helper. If `stripe.secret-key` is not configured
 * it returns synthetic ids. Real Stripe integration can be implemented here using
 * stripe-java SDK.
 */
@Service
public class StripeSubscriptionService {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    public String createSubscription(User user, String planId) throws Exception {
        if (stripeSecretKey == null || stripeSecretKey.isBlank() || stripeSecretKey.equals("sk_test_")) {
            return "sub_fake_" + System.currentTimeMillis();
        }
        // TODO: implement real Stripe customer + subscription creation using stripe-java SDK
        return "sub_fake_" + System.currentTimeMillis();
    }

    public boolean cancelSubscription(String stripeSubscriptionId) throws Exception {
        if (stripeSecretKey == null || stripeSecretKey.isBlank() || stripeSecretKey.equals("sk_test_")) {
            return true;
        }
        // TODO: implement cancel via stripe-java
        return true;
    }
}
