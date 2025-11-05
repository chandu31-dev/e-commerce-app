package com.catchy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.dto.LoginRequest;
import com.catchy.dto.SignupRequest;
import com.catchy.model.User;
import com.catchy.repository.UserRepository;
import com.catchy.util.JwtUtil;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MailService mailService;

    public java.util.Optional<com.catchy.model.User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public com.catchy.model.User saveUser(com.catchy.model.User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(User.Role.USER);
        user.setVerified(false);

        return userRepository.save(user);
    }

    public void signupAndSendVerification(SignupRequest signupRequest, String appUrl) {
        User user = signup(signupRequest);
        // create token and send verification email
        var vt = tokenService.createVerificationTokenForUser(user);
        String link = appUrl + "/verify?token=" + vt.getToken();
        String subject = "Verify your Catchy account";
        String text = "Please verify your account by clicking the link: " + link;
        mailService.sendVerificationEmail(user.getEmail(), subject, text);
    }

    public String login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

