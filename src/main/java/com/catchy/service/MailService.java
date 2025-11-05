package com.catchy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String subject, String text) {
        if (mailSender == null) {
            // Fallback: log to console
            System.out.println("[MailService] Verification email to=" + to + " subject=" + subject + " text=" + text);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendResetEmail(String to, String subject, String text) {
        if (mailSender == null) {
            System.out.println("[MailService] Reset email to=" + to + " subject=" + subject + " text=" + text);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
