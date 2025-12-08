package com.catchy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.Product;
import com.catchy.model.Review;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.ProductService;
import com.catchy.service.ReviewService;
import com.catchy.service.ReviewThrottleService;

@Controller
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewThrottleService reviewThrottleService;

    @PostMapping("/product/api/review")
    @ResponseBody
    public ResponseEntity<?> addReview(@RequestParam Long productId, @RequestParam Integer rating, @RequestParam(required=false) String comment) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body("Please login");
            Product product = productService.getProductById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
            // enforce rate-limits
            reviewThrottleService.checkAllowed(user.getId(), productId);
            Review r = reviewService.addReview(product, user, comment, rating);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin endpoints for moderation
    @org.springframework.web.bind.annotation.GetMapping("/admin/api/reviews")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<?> listReviews(@org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
        try {
            if (status == null) {
                return ResponseEntity.ok(reviewService.getReviewsForProduct(null));
            }
            Review.ModerationStatus st = Review.ModerationStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(reviewService.findByStatus(st));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/api/reviews/{id}/approve")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<?> approveReview(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam(required = false) String note) {
        try {
            Review r = reviewService.moderateReview(id, Review.ModerationStatus.APPROVED, note);
            return ResponseEntity.ok(r);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/api/reviews/{id}/reject")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<?> rejectReview(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam(required = false) String note) {
        try {
            Review r = reviewService.moderateReview(id, Review.ModerationStatus.REJECTED, note);
            return ResponseEntity.ok(r);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
