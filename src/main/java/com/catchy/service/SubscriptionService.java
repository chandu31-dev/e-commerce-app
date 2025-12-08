package com.catchy.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Subscription;
import com.catchy.model.User;
import com.catchy.repository.SubscriptionRepository;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public Subscription createLocalSubscription(User user, String planId, String status, long currentPeriodEndEpochSeconds) {
        Subscription s = new Subscription(user, planId, status);
        if (currentPeriodEndEpochSeconds > 0) {
            s.setCurrentPeriodEnd(LocalDateTime.ofInstant(Instant.ofEpochSecond(currentPeriodEndEpochSeconds), java.time.ZoneId.systemDefault()));
        }
        return subscriptionRepository.save(s);
    }

    public List<Subscription> getSubscriptionsForUser(User user) {
        return subscriptionRepository.findByUser(user);
    }

    public Subscription findById(Long id) {
        return subscriptionRepository.findById(id).orElse(null);
    }

    @Transactional
    public void markCancelled(Subscription s) {
        s.setStatus("cancelled");
        s.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(s);
    }

    public Subscription findByStripeId(String stripeId) {
        // Stripe removed: keep stub that returns null
        return null;
    }
}
