package com.catchy.controller;

import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String profilePage(Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            model.addAttribute("user", user);
            return "profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @PutMapping("/api/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestParam(required = false) String name,
                                                              @RequestParam(required = false) String email,
                                                              @RequestParam(required = false) String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }
            User updatedUser = userService.updateUser(currentUser.getId(), name, email, password);
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}

