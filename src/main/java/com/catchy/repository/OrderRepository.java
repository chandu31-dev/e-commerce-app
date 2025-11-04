package com.catchy.repository;

import com.catchy.model.Order;
import com.catchy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllOrderByOrderDateDesc();
}

