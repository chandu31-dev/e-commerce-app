package com.catchy.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.catchy.model.Product;
import com.catchy.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    Page<Review> findByProduct(Product product, Pageable pageable);
    Page<Review> findByProductAndStatus(Product product, Review.ModerationStatus status, Pageable pageable);
    java.util.List<Review> findByStatus(Review.ModerationStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(@Param("product") Product product);
}
