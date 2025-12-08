package com.catchy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Coupon;
import com.catchy.repository.CouponRepository;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private com.catchy.repository.OrderRepository orderRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public void incrementUsage(Coupon c) {
        if (c == null) return;
        c.setUsageCount((c.getUsageCount() == null ? 0 : c.getUsageCount()) + 1);
        couponRepository.save(c);
    }

    /**
     * Calculate discount for given coupon and cart. Throws RuntimeException when coupon is invalid.
     */
    public java.math.BigDecimal calculateDiscount(Coupon coupon, com.catchy.model.User user, java.util.List<com.catchy.model.CartItem> cartItems, java.math.BigDecimal cartTotal) {
        if (coupon == null) throw new RuntimeException("Coupon not found");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (!coupon.isActive() || coupon.getValidFrom().isAfter(now) || coupon.getValidUntil().isBefore(now)) {
            throw new RuntimeException("Coupon is not valid at this time");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached");
        }
        if (coupon.getPerUserLimit() != null && user != null) {
            int used = orderRepository.countByUserAndCouponCode(user, coupon.getCode());
            if (used >= coupon.getPerUserLimit()) throw new RuntimeException("Coupon per-user limit reached");
        }
        if (coupon.getMinOrderAmount() != null && cartTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new RuntimeException("Order total is less than coupon minimum amount");
        }

        // Determine eligible subtotal based on applicable categories/products
        java.math.BigDecimal eligible = java.math.BigDecimal.ZERO;
        java.util.Set<String> cats = new java.util.HashSet<>();
        if (coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isBlank()) {
            for (String s : coupon.getApplicableCategories().split(",")) cats.add(s.trim().toLowerCase());
        }
        java.util.Set<Long> pids = new java.util.HashSet<>();
        if (coupon.getApplicableProductIds() != null && !coupon.getApplicableProductIds().isBlank()) {
            for (String s : coupon.getApplicableProductIds().split(",")) {
                try { pids.add(Long.parseLong(s.trim())); } catch (Exception ex) {}
            }
        }

        boolean hasRestrictions = !cats.isEmpty() || !pids.isEmpty();
        for (com.catchy.model.CartItem ci : cartItems) {
            com.catchy.model.Product p = ci.getProduct();
            java.math.BigDecimal line = p.getPrice().multiply(java.math.BigDecimal.valueOf(ci.getQuantity()));
            if (!hasRestrictions) {
                eligible = eligible.add(line);
            } else {
                if (pids.contains(p.getId()) || cats.contains(p.getCategory() != null ? p.getCategory().toLowerCase() : "")) {
                    eligible = eligible.add(line);
                }
            }
        }

        if (eligible.compareTo(java.math.BigDecimal.ZERO) <= 0) throw new RuntimeException("Coupon does not apply to any items in the cart");

        java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
        if (coupon.getFixedAmount() != null) {
            discount = coupon.getFixedAmount();
            if (discount.compareTo(eligible) > 0) discount = eligible;
        } else if (coupon.getDiscountPercent() != null) {
            discount = eligible.multiply(java.math.BigDecimal.valueOf(coupon.getDiscountPercent()/100.0));
        }
        if (discount.compareTo(cartTotal) > 0) discount = cartTotal;
        return discount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Transactional
    public Coupon createCoupon(String code, Double discountPercent, LocalDateTime from, LocalDateTime until) {
        Coupon c = new Coupon(code, discountPercent, from, until);
        return couponRepository.save(c);
    }

    @Transactional
    public Coupon saveCoupon(Coupon c) {
        return couponRepository.save(c);
    }

    @Transactional
    public Coupon deactivateCoupon(Long id) {
        Coupon c = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
        c.setActive(false);
        return couponRepository.save(c);
    }
}
