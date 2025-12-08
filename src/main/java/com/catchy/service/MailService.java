package com.catchy.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@ConditionalOnProperty(prefix = "features", name = "addresses-only", havingValue = "false", matchIfMissing = false)
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void sendVerificationEmail(String to, String subject, String text) {
        // Prefer HTML template when template engine and mail sender available
        if (mailSender != null && templateEngine != null) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
                Context ctx = new Context();
                ctx.setVariables(Map.of("name", to, "body", text));
                String html = templateEngine.process("email/verification", ctx);
                helper.setTo(to);
                if (fromAddress != null && !fromAddress.isBlank()) helper.setFrom(fromAddress);
                helper.setSubject(subject);
                helper.setText(html, true);
                CompletableFuture.runAsync(() -> {
                    try {
                        mailSender.send(mime);
                    } catch (Exception ex) {
                        logger.error("[MailService] Failed to send verification email to {}: {}", to, ex.getMessage());
                    }
                });
                return;
            } catch (Exception e) {
                logger.warn("[MailService] HTML email failed, falling back to text: {}", e.getMessage());
            }
        }

        if (mailSender == null) {
            // Fallback: log to console and file
            logger.info("[MailService] Email to={} subject={}", to, subject);
            logger.debug("Email body: {}", text);
            try {
                Path out = Path.of("target", "verification-link.txt");
                Files.createDirectories(out.getParent());
                Files.writeString(out, text + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                logger.warn("[MailService] Failed to write verification link to file: {}", e.getMessage());
            }
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject(subject);
        message.setText(text);
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    mailSender.send(message);
                } catch (Exception ex) {
                    logger.error("[MailService] Failed to send verification email to {}: {}", to, ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("[MailService] Failed to schedule verification email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOrderConfirmationEmail(String to, String subject, String text) {
        if (mailSender != null && templateEngine != null) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
                Context ctx = new Context();
                ctx.setVariables(Map.of("body", text));
                String html = templateEngine.process("email/order-confirmation", ctx);
                helper.setTo(to);
                if (fromAddress != null && !fromAddress.isBlank()) helper.setFrom(fromAddress);
                helper.setSubject(subject);
                helper.setText(html, true);
                CompletableFuture.runAsync(() -> {
                    try { mailSender.send(mime); } catch (Exception ex) { logger.error("[MailService] Failed to send order email: {}", ex.getMessage()); }
                });
                return;
            } catch (Exception e) {
                logger.warn("[MailService] HTML order email failed, falling back: {}", e.getMessage());
            }
        }

        if (mailSender == null) {
            logger.info("[MailService] ORDER CONFIRMATION EMAIL to={} subject={}", to, subject);
            logger.debug("Order email body: {}", text);
            try {
                Path out = Path.of("target", "order-confirmation.txt");
                Files.createDirectories(out.getParent());
                Files.writeString(out, "TO: " + to + "\nSUBJECT: " + subject + "\n\n" + text + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                logger.warn("[MailService] Failed to write order confirmation to file: {}", e.getMessage());
            }
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject(subject);
        message.setText(text);
        try {
            CompletableFuture.runAsync(() -> {
                try { mailSender.send(message); } catch (Exception ex) { logger.error("[MailService] Failed to send order confirmation to {}: {}", to, ex.getMessage()); }
            });
        } catch (Exception e) {
            logger.error("[MailService] Failed to schedule order confirmation to {}: {}", to, e.getMessage());
        }
    }

    public void sendVendorNotificationEmail(String to, String subject, String text) {
        if (mailSender != null && templateEngine != null) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
                Context ctx = new Context();
                ctx.setVariables(Map.of("body", text));
                String html = templateEngine.process("email/vendor-notification", ctx);
                helper.setTo(to);
                if (fromAddress != null && !fromAddress.isBlank()) helper.setFrom(fromAddress);
                helper.setSubject(subject);
                helper.setText(html, true);
                CompletableFuture.runAsync(() -> {
                    try { mailSender.send(mime); } catch (Exception ex) { logger.error("[MailService] Failed to send vendor email: {}", ex.getMessage()); }
                });
                return;
            } catch (Exception e) {
                logger.warn("[MailService] HTML vendor email failed, falling back: {}", e.getMessage());
            }
        }

        if (mailSender == null) {
            logger.info("[MailService] VENDOR NOTIFICATION EMAIL to={} subject={}", to, subject);
            logger.debug("Vendor notification body: {}", text);
            try {
                Path out = Path.of("target", "vendor-notifications.txt");
                Files.createDirectories(out.getParent());
                Files.writeString(out, "TO: " + to + "\nSUBJECT: " + subject + "\n\n" + text + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                logger.warn("[MailService] Failed to write vendor notification to file: {}", e.getMessage());
            }
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject(subject);
        message.setText(text);
        try {
            CompletableFuture.runAsync(() -> {
                try { mailSender.send(message); } catch (Exception ex) { logger.error("[MailService] Failed to send vendor notification to {}: {}", to, ex.getMessage()); }
            });
        } catch (Exception e) {
            logger.error("[MailService] Failed to schedule vendor notification to {}: {}", to, e.getMessage());
        }
    }

    public void sendResetEmail(String to, String subject, String text) {
        if (mailSender != null && templateEngine != null) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
                Context ctx = new Context();
                ctx.setVariables(Map.of("body", text));
                String html = templateEngine.process("email/reset", ctx);
                helper.setTo(to);
                if (fromAddress != null && !fromAddress.isBlank()) helper.setFrom(fromAddress);
                helper.setSubject(subject);
                helper.setText(html, true);
                CompletableFuture.runAsync(() -> { try { mailSender.send(mime); } catch (Exception ex) { logger.error("[MailService] Failed to send reset email: {}", ex.getMessage()); } });
                return;
            } catch (Exception e) {
                logger.warn("[MailService] HTML reset email failed, falling back: {}", e.getMessage());
            }
        }

        if (mailSender == null) {
            logger.info("[MailService] Reset email to={} subject={} text={}", to, subject, text);
            try {
                Path out = Path.of("target", "reset-link.txt");
                Files.createDirectories(out.getParent());
                Files.writeString(out, text + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                logger.warn("[MailService] Failed to write reset link to file: {}", e.getMessage());
            }
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject(subject);
        message.setText(text);
        try {
            CompletableFuture.runAsync(() -> { try { mailSender.send(message); } catch (Exception ex) { logger.error("[MailService] Failed to send reset email to {}: {}", to, ex.getMessage()); } });
        } catch (Exception e) {
            logger.error("[MailService] Failed to schedule reset email to {}: {}", to, e.getMessage());
        }
    }
}
