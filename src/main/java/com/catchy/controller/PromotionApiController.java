package com.catchy.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.Coupon;
import com.catchy.service.CouponService;

@Controller
public class PromotionApiController {
    @Autowired
    private CouponService couponService;

    @GetMapping("/promotions/api/coupons")
    @ResponseBody
    public ResponseEntity<List<Coupon>> listCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping("/promotions/api/coupons")
    @ResponseBody
    public ResponseEntity<?> createCoupon(@RequestParam String code,
                                          @RequestParam Double discountPercent,
                                          @RequestParam String validFrom,
                                          @RequestParam String validUntil,
                                          @RequestParam(required = false) Integer usageLimit,
                                          @RequestParam(required = false) Integer perUserLimit,
                                          @RequestParam(required = false) String applicableCategories,
                                          @RequestParam(required = false) String applicableProductIds,
                                          @RequestParam(required = false) String minOrderAmount,
                                          @RequestParam(required = false) String fixedAmount
                                          ) {
        try {
            LocalDateTime from = LocalDateTime.parse(validFrom);
            LocalDateTime until = LocalDateTime.parse(validUntil);
            Coupon c = couponService.createCoupon(code, discountPercent, from, until);
            if (usageLimit != null) c.setUsageLimit(usageLimit);
            if (perUserLimit != null) c.setPerUserLimit(perUserLimit);
            if (applicableCategories != null) c.setApplicableCategories(applicableCategories);
            if (applicableProductIds != null) c.setApplicableProductIds(applicableProductIds);
            if (minOrderAmount != null) c.setMinOrderAmount(new java.math.BigDecimal(minOrderAmount));
            if (fixedAmount != null) c.setFixedAmount(new java.math.BigDecimal(fixedAmount));
            couponService.saveCoupon(c);
            return ResponseEntity.ok(c);
        } catch (DateTimeParseException dpe) {
            return ResponseEntity.badRequest().body("Invalid date format");
        }
    }
}
