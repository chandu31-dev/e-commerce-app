package com.catchy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catchy.model.CartItem;
import com.catchy.model.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProductId(User user, Long productId);
    void deleteByUser(User user);
    
    List<CartItem> findBySessionId(String sessionId);
    Optional<CartItem> findBySessionIdAndProductId(String sessionId, Long productId);
    void deleteBySessionId(String sessionId);
}

