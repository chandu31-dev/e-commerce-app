package com.catchy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.AbandonedCartReminder;
import com.catchy.model.CartItem;
import com.catchy.model.User;
import com.catchy.repository.AbandonedCartReminderRepository;
import com.catchy.repository.CartItemRepository;
import com.catchy.repository.UserRepository;

@Service
public class AbandonedCartService {

    private static final Logger log = LoggerFactory.getLogger(AbandonedCartService.class);

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AbandonedCartReminderRepository abandonedCartReminderRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Value("${abandoned.cart.threshold.hours:24}")
    private Integer thresholdHours;

    @Value("${abandoned.cart.check-rate-ms:3600000}")
    private Long checkRateMs;

    @Scheduled(fixedRateString = "${abandoned.cart.check-rate-ms:3600000}")
    public void scheduledCheck() {
        processAbandonedCarts();
    }

    @Transactional
    public void processAbandonedCarts() {
        LocalDateTime threshold;
        if (thresholdHours <= 0) {
            // If threshold is 0 or negative, use current time (all carts are "abandoned")
            threshold = LocalDateTime.now();
        } else {
            threshold = LocalDateTime.now().minusHours(thresholdHours);
        }
        log.info("Processing abandoned carts with threshold: {}", threshold);
        List<User> users = cartItemRepository.findDistinctUsersWithCartUpdatedBefore(threshold);
        log.info("Found {} users with abandoned carts", users.size());
        for (User u : users) {
            try {
                // skip if user has orders
                if (u.getOrders() != null && !u.getOrders().isEmpty()) continue;
                // skip if user has disabled marketing? Not implemented; skip for now

                var lastReminderOpt = abandonedCartReminderRepository.findTopByUserOrderByCreatedAtDesc(u);
                if (lastReminderOpt.isPresent()) {
                    var last = lastReminderOpt.get();
                    // only send new reminder if last sent more than threshold hours ago
                    if (last.getCreatedAt().isAfter(threshold)) continue;
                }

                List<CartItem> items = cartItemRepository.findByUser(u);
                if (items == null || items.isEmpty()) continue;

                String cartSnapshot = items.stream()
                        .map(ci -> String.format("%s x %d", ci.getProduct().getName(), ci.getQuantity()))
                        .collect(Collectors.joining("\n"));

                String token = UUID.randomUUID().toString();
                AbandonedCartReminder reminder = new AbandonedCartReminder(u, token, cartSnapshot);
                abandonedCartReminderRepository.save(reminder);
                log.info("Saved initial abandoned cart reminder for user {} token={}", u.getEmail(), token);

                String resumeLink = "/cart/resume?token=" + token;
                String html = "You left items in your cart:\n" + cartSnapshot + "\n\nResume: " + resumeLink;
                try {
                    mailService.sendAbandonedCartEmail(u.getEmail(), "Complete your cart at Catchy", html, token);
                    log.info("Sent abandoned cart email to {}", u.getEmail());
                } catch (Exception ex) {
                    log.error("Failed to send email to {}: {}", u.getEmail(), ex.getMessage());
                }

                reminder.setSentAt(LocalDateTime.now());
                reminder.setReminderCount(reminder.getReminderCount() + 1);
                abandonedCartReminderRepository.save(reminder);
                log.info("Updated abandoned cart reminder for user {} token={} sentAt={} count={}", u.getEmail(), token, reminder.getSentAt(), reminder.getReminderCount());
            } catch (Exception e) {
                log.error("Error processing abandoned cart for user {}: {}", u.getEmail(), e.getMessage(), e);
            }
        }
    }
}
