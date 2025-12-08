package com.catchy.payout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.catchy.model.Payout;

/**
 * Lightweight Stripe payout provider skeleton.
 * If `stripe.secret-key` is not configured, this provider falls back to a fake reference
 * but is the place to implement real Stripe/Connect transfer logic.
 */
@Component("stripePayoutProvider")
public class StripePayoutProvider implements PayoutProvider {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Override
    public String pay(Payout payout) throws Exception {
        // If stripe key not configured, return a synthetic reference.
        if (stripeSecretKey == null || stripeSecretKey.isBlank() || stripeSecretKey.equals("sk_test_")) {
            return "STRIPE-FAKE-" + payout.getId() + "-" + System.currentTimeMillis();
        }

        // TODO: implement real Stripe Connect transfer using stripe-java SDK.
        // Keep a safe fallback for now.
        return "STRIPE-FAKE-" + payout.getId() + "-" + System.currentTimeMillis();
    }
}
