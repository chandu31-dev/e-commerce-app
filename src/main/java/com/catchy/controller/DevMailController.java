package com.catchy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.service.MailService;

@RestController
@RequestMapping("/dev/mail")
@ConditionalOnProperty(prefix = "features", name = "addresses-only", havingValue = "false", matchIfMissing = false)
public class DevMailController {
    @Autowired
    private MailService mailService;

    // Temporary dev endpoint to send test emails. Disabled when addresses-only feature is enabled.
    @PostMapping("/send-test")
    @ResponseBody
    @ConditionalOnProperty(prefix = "features", name = "addresses-only", havingValue = "false", matchIfMissing = false)
    public ResponseEntity<String> sendTest(@RequestBody String to) {
        mailService.sendVerificationEmail(to, "Test Email", "This is a test");
        return ResponseEntity.ok("Queued");
    }
}
