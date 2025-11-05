package com.catchy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.dto.LoginRequest;
import com.catchy.dto.SignupRequest;
import com.catchy.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private com.catchy.service.TokenService tokenService;
    @Autowired
    private com.catchy.service.MailService mailService;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/api/auth/signup")
    @ResponseBody
    public String signup(@Valid @RequestBody SignupRequest signupRequest, BindingResult result) {
        if (result.hasErrors()) {
            return "error";
        }
        try {
            // Build app URL from request origin or default
            String appUrl = "http://localhost:8080";
            authService.signupAndSendVerification(signupRequest, appUrl);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        var vt = tokenService.validateVerificationToken(token);
        if (vt == null) {
            model.addAttribute("message", "Invalid or expired verification token");
            return "verification-result";
        }
        // Mark user verified and delete token
        var user = vt.getUser();
        user.setVerified(true);
        authService.saveUser(user);
        // delete token
        tokenService.deleteVerificationToken(vt);
        model.addAttribute("message", "Email verified successfully. You can login now.");
        return "verification-result";
    }

    @PostMapping("/api/auth/request-reset")
    @ResponseBody
    public String requestPasswordReset(@RequestParam String email) {
        try {
            var userOpt = authService.findByEmail(email);
            if (userOpt.isEmpty()) return "ok"; // no info leakage
            var prt = tokenService.createPasswordResetTokenForUser(userOpt.get());
            String link = "http://localhost:8080/reset-password?token=" + prt.getToken();
            mailService.sendResetEmail(email, "Password reset", "Reset link: " + link);
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/api/auth/reset")
    @ResponseBody
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            var prt = tokenService.validatePasswordResetToken(token);
            if (prt == null) return "invalid";
            var user = prt.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            // save user
            authService.saveUser(user);
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public String login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            String token = authService.login(loginRequest);
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 hours
            response.addCookie(cookie);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}

