package com.catchy.controller;

import com.catchy.dto.LoginRequest;
import com.catchy.dto.SignupRequest;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

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
            authService.signup(signupRequest);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
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

