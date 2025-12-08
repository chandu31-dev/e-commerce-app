package com.catchy.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ReviewThrottleService {

    // last review time per user:product -> epoch millis
    private final Map<String, Long> lastPerProduct = new ConcurrentHashMap<>();

    // recent review timestamps per user for rate count
    private final Map<Long, List<Long>> recentByUser = new ConcurrentHashMap<>();

    // Minimum seconds between two reviews for same product by same user
    private final long minSecondsPerProduct = Long.parseLong(System.getProperty("reviews.min-seconds-per-product", "30"));

    // Max reviews per rolling window (seconds)
    private final int maxPerWindow = Integer.parseInt(System.getProperty("reviews.max-per-window", "5"));
    private final long windowSeconds = Long.parseLong(System.getProperty("reviews.window-seconds", "3600"));

    public synchronized void checkAllowed(Long userId, Long productId) {
        long now = Instant.now().toEpochMilli();
        String key = userId + ":" + productId;
        Long last = lastPerProduct.get(key);
        if (last != null && (now - last) < minSecondsPerProduct * 1000L) {
            throw new RuntimeException("Please wait before submitting another review for this product");
        }

        // cleanup and count
        List<Long> list = recentByUser.computeIfAbsent(userId, k -> new ArrayList<>());
        long cutoff = now - windowSeconds * 1000L;
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) {
            Long t = it.next();
            if (t < cutoff) it.remove();
        }
        if (list.size() >= maxPerWindow) {
            throw new RuntimeException("Review rate limit exceeded. Try again later.");
        }

        // record
        list.add(now);
        lastPerProduct.put(key, now);
    }
}
