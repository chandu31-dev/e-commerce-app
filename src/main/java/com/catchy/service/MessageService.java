package com.catchy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Message;
import com.catchy.model.User;
import com.catchy.repository.MessageRepository;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public List<Message> getThread(User a, User b) {
        return messageRepository.findThread(a, b);
    }

    public List<Message> getInbox(User user) {
        return messageRepository.findByReceiverOrderBySentAtDesc(user);
    }

    @Transactional
    public Message sendMessage(User sender, User receiver, String content) {
        Message m = new Message(sender, receiver, content);
        return messageRepository.save(m);
    }
}
