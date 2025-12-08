package com.catchy.service;

import java.time.LocalDateTime;
import java.util.Optional;
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

    public VerificationToken createVerificationTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken(token, user, LocalDateTime.now().plusHours(24));
        return verificationTokenRepository.save(vt);
    }

    public VerificationToken validateVerificationToken(String token) {
        Optional<VerificationToken> vt = verificationTokenRepository.findByToken(token);
        if (vt.isPresent() && vt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            return vt.get();
        }
        return null;
    }

    public void deleteVerificationToken(VerificationToken token) {
        if (token != null) verificationTokenRepository.delete(token);
    }

    public PasswordResetToken createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(2));
        return passwordResetTokenRepository.save(prt);
    }

    public PasswordResetToken validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> prt = passwordResetTokenRepository.findByToken(token);
        if (prt.isPresent() && prt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            return prt.get();
        }
        return null;
    }

    public void deletePasswordResetToken(PasswordResetToken token) {
        if (token != null) passwordResetTokenRepository.delete(token);
    }

    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
