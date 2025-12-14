package com.catchy.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.catchy.model.CartItem;
import com.catchy.model.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProductId(User user, Long productId);
    void deleteByUser(User user);

    @Query("select distinct c.user from CartItem c where c.updatedAt < :time")
    List<User> findDistinctUsersWithCartUpdatedBefore(@Param("time") LocalDateTime time);
}

