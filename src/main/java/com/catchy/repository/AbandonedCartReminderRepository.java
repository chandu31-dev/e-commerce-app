package com.catchy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catchy.model.AbandonedCartReminder;
import com.catchy.model.User;

@Repository
public interface AbandonedCartReminderRepository extends JpaRepository<AbandonedCartReminder, Long> {
    Optional<AbandonedCartReminder> findByToken(String token);
    Optional<AbandonedCartReminder> findTopByUserOrderByCreatedAtDesc(User user);
}
