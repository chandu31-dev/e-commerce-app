package com.catchy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catchy.model.Order;
import com.catchy.model.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.user.id = :userId")
    int countByProductIdAndOrderUserId(@org.springframework.data.repository.query.Param("productId") Long productId, @org.springframework.data.repository.query.Param("userId") Long userId);
}

