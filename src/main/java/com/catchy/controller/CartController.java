package com.catchy.controller;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.CartItem;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    @Autowired
    private com.catchy.repository.AbandonedCartReminderRepository abandonedCartReminderRepository;

    @GetMapping
    public String cartPage(Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            List<CartItem> cartItems = cartService.getCartItems(user);
            BigDecimal total = cartService.getCartTotal(user);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
            return "cart";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam Long productId, 
                                                          @RequestParam(defaultValue = "1") Integer quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }
            cartService.addToCart(user, productId, quantity);
            response.put("success", true);
            response.put("message", "Product added to cart");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(@PathVariable Long id, 
                                                               @RequestParam Integer quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }
            cartService.updateCartItemQuantity(user, id, quantity);
            response.put("success", true);
            response.put("message", "Cart updated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/api/remove/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }
            cartService.removeFromCart(user, id);
            response.put("success", true);
            response.put("message", "Item removed from cart");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<List<CartItem>> getCartItems() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(cartService.getCartItems(user));
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/resume")
    public String resumeCart(@RequestParam String token) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                String redirect = URLEncoder.encode("/cart/resume?token=" + token, StandardCharsets.UTF_8);
                return "redirect:/login?redirect=" + redirect;
            }
            Optional<com.catchy.model.AbandonedCartReminder> r = abandonedCartReminderRepository.findByToken(token);
            if (r.isPresent()) {
                var rem = r.get();
                if (rem.getUser().getId().equals(user.getId())) {
                    rem.setRecovered(true);
                    abandonedCartReminderRepository.save(rem);
                }
            }
            return "redirect:/cart";
        } catch (Exception e) {
            return "redirect:/cart";
        }
    }
}

