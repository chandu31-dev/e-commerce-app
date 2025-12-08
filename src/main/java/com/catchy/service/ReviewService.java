package com.catchy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Product;
import com.catchy.model.Review;
import com.catchy.model.User;
import com.catchy.repository.ReviewRepository;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private com.catchy.repository.OrderItemRepository orderItemRepository;

    public List<Review> getReviewsForProduct(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public org.springframework.data.domain.Page<Review> getReviewsForProductPage(Product product, org.springframework.data.domain.Pageable pageable) {
        return reviewRepository.findByProduct(product, pageable);
    }

    public double getAverageRating(Product product) {
        Double avg = reviewRepository.findAverageRatingByProduct(product);
        return avg == null ? 0.0 : avg.doubleValue();
    }

    @Transactional
    public Review addReview(Product product, User user, String comment, Integer rating) {
        Review review = new Review(product, user, comment, rating);
        // mark verifiedPurchase if user has previously purchased this product
        try {
            int purchasedCount = orderItemRepository.countByProductIdAndOrderUserId(product.getId(), user.getId());
            if (purchasedCount > 0) review.setVerifiedPurchase(true);
        } catch (Exception ex) {
            // ignore
        }
        return reviewRepository.save(review);
    }

    public java.util.List<Review> findByStatus(Review.ModerationStatus status) {
        return reviewRepository.findByStatus(status);
    }

    @Transactional
    public Review moderateReview(Long id, Review.ModerationStatus status, String note) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        r.setStatus(status);
        r.setModerationNote(note);
        r.setModeratedAt(java.time.LocalDateTime.now());
        return reviewRepository.save(r);
    }
}
