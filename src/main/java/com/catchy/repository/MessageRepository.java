package com.catchy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.catchy.model.Message;
import com.catchy.model.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender = :u1 AND m.receiver = :u2) OR (m.sender = :u2 AND m.receiver = :u1) ORDER BY m.sentAt ASC")
    List<Message> findThread(@Param("u1") User u1, @Param("u2") User u2);

    List<Message> findByReceiverOrderBySentAtDesc(User receiver);
}
