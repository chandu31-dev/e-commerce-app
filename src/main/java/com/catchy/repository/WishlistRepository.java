package com.catchy.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catchy.model.User;
import com.catchy.model.WishlistItem;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUser(User user);
    Page<WishlistItem> findByUser(User user, Pageable pageable);
    void deleteByUserAndProductId(User user, Long productId);
    boolean existsByUserAndProductId(User user, Long productId);
}
