package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.Message;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.MessageService;

import java.util.List;

@Controller
public class MessagingController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private AuthService authService;

    @GetMapping("/messages")
    public String messagesPage() {
        return "messages";
    }

    @GetMapping("/messages/api/thread/{userId}")
    @ResponseBody
    public ResponseEntity<List<Message>> getThread(@PathVariable Long userId) {
        try {
            User current = authService.getCurrentUser();
            if (current == null) return ResponseEntity.status(401).build();
            // load other user entity reference
            User other = new User(); other.setId(userId);
            List<Message> thread = messageService.getThread(current, other);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/messages/api/send")
    @ResponseBody
    public ResponseEntity<Message> sendMessage(@RequestParam Long toUserId, @RequestParam String content) {
        try {
            User current = authService.getCurrentUser();
            if (current == null) return ResponseEntity.status(401).build();
            User other = new User(); other.setId(toUserId);
            Message m = messageService.sendMessage(current, other, content);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
