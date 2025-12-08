package com.catchy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.catchy.model.PasswordResetToken;
import com.catchy.model.User;
import com.catchy.model.VerificationToken;
import com.catchy.repository.UserRepository;
import com.catchy.service.AuthService;
import com.catchy.service.MailService;
import com.catchy.service.TokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired(required = false)
    private TokenService tokenService;

    @Autowired(required = false)
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.catchy.service.CartService cartService;

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
    public String signupPage(@RequestParam(value = "role", required = false) String role, Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        model.addAttribute("role", role == null ? "USER" : role);
        return "signup";
    }



    @GetMapping("/api/auth/verify")
    public String verifyAccount(@RequestParam("token") String token, Model model) {
        if (tokenService == null) {
            model.addAttribute("message", "Verification not available");
            return "verification-result";
        }
        VerificationToken vt = tokenService.validateVerificationToken(token);
        if (vt == null) {
            model.addAttribute("message", "Invalid or expired verification token");
            return "verification-result";
        }
        User user = vt.getUser();
        user.setEnabled(true);
        authService.saveUser(user);
        tokenService.deleteVerificationToken(vt);
        model.addAttribute("message", "Your account has been verified. You can now log in.");
        return "verification-result";
    }

    @PostMapping("/api/auth/request-reset")
    public String requestPasswordReset(@RequestParam("email") String email, Model model) {
        var opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            model.addAttribute("message", "If an account exists we sent a reset link.");
            return "request-reset";
        }
        User user = opt.get();
        if (tokenService != null && mailService != null) {
            var prt = tokenService.createPasswordResetTokenForUser(user);
            String link = "http://localhost:8080/reset-password?token=" + prt.getToken();
            String body = "Hi " + user.getName() + ",\n\nUse the link below to reset your password:\n" + link + "\n\nIf you did not request this, ignore.";
            mailService.sendResetEmail(user.getEmail(), "Reset your Catchy password", body);
        }
        model.addAttribute("message", "If an account exists we sent a reset link.");
        return "request-reset";
    }

    @PostMapping("/api/auth/reset")
    public String resetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        if (tokenService == null) {
            model.addAttribute("message", "Password reset not available");
            return "reset-password";
        }
        PasswordResetToken prt = tokenService.validatePasswordResetToken(token);
        if (prt == null) {
            model.addAttribute("message", "Invalid or expired reset token");
            return "reset-password";
        }
        User user = prt.getUser();
        // Update password via AuthService helper
        authService.updatePassword(user, password);
        tokenService.deletePasswordResetToken(prt);
        if (mailService != null) {
            mailService.sendResetEmail(user.getEmail(), "Your password was changed", "Your Catchy account password was successfully changed.");
        }
        model.addAttribute("message", "Password reset successful. You can now log in.");
        return "reset-password";
    }

    @PostMapping("/api/auth/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid input"));
        }
        try {
            authService.signup(signupRequest);
            // After successful signup, redirect user to login page to authenticate
            return ResponseEntity.ok(Map.of("success", true, "redirectUrl", "/login"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.login(loginRequest);

            // Build cookie using ResponseCookie so browser honors it with fetch(credentials: 'include')
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("JWT_TOKEN", token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(86400)
                    .sameSite("Lax")
                    .build();

            // Get user role for redirect
            var user = userRepository.findByEmail(loginRequest.getEmail());
            String redirectUrl = "/buyer/home";
            if (user.isPresent() && user.get().getRole().equals(com.catchy.model.User.Role.VENDOR)) {
                redirectUrl = "/vendor/home";
            }

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("success", true);
            body.put("token", token);
            body.put("redirectUrl", redirectUrl);

            return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(body);
        } catch (Exception e) {
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("success", false);
            err.put("message", "error: " + e.getMessage());
            return ResponseEntity.status(401).body(err);
        }
    }

    @PostMapping("/api/auth/merge-cart")
    @ResponseBody
    public ResponseEntity<?> mergeCart(@RequestBody java.util.List<com.catchy.dto.CartMergeItem> items) {
        try {
            com.catchy.model.User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Please login"));
            int merged = cartService.mergeGuestCart(user, items);
            return ResponseEntity.ok(Map.of("success", true, "merged", merged));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
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

