package com.catchy.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catchy.model.PasswordResetToken;
import com.catchy.model.User;
import com.catchy.model.VerificationToken;
import com.catchy.repository.PasswordResetTokenRepository;
import com.catchy.repository.VerificationTokenRepository;

@Service
public class TokenService {
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public void deleteVerificationToken(VerificationToken token) {
        verificationTokenRepository.delete(token);
    }

    public void deletePasswordResetToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }

    public VerificationToken createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken(token, user, LocalDateTime.now().plusDays(2));
        return verificationTokenRepository.save(vt);
    }

    public PasswordResetToken createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(24));
        return passwordResetTokenRepository.save(prt);
    }

    public VerificationToken validateVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    public PasswordResetToken validatePasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    // Scheduled cleanup for expired tokens
    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "PT1H")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        verificationTokenRepository.deleteByExpiryDateBefore(now);
        passwordResetTokenRepository.deleteByExpiryDateBefore(now);
    }

    // create convenience method to create tokens with return types used elsewhere
    public VerificationToken createVerificationTokenForUser(User user) {
        return createVerificationToken(user);
    }

    public PasswordResetToken createPasswordResetTokenForUser(User user) {
        return createPasswordResetToken(user);
    }
}
