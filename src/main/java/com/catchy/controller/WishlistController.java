package com.catchy.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.ProductService;
import com.catchy.service.WishlistService;

@Controller
public class WishlistController {

    @GetMapping("/wishlist")
    public String wishlist() {
        return "wishlist";
    }

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductService productService;

    @GetMapping("/wishlist/api/items")
    @ResponseBody
    public ResponseEntity<?> getWishlist(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body("Please login");
            org.springframework.data.domain.Page<?> p = wishlistService.getWishlistPage(user, org.springframework.data.domain.PageRequest.of(page, size));
            return ResponseEntity.ok(p);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/wishlist/api/add")
    @ResponseBody
    public ResponseEntity<?> addToWishlist(@RequestParam Long productId) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body("Please login");
            Product product = productService.getProductById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
            wishlistService.addToWishlist(user, product);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/wishlist/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlist(@RequestParam Long productId) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body("Please login");
            wishlistService.removeFromWishlist(user, productId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/wishlist/api/ids")
    @ResponseBody
    public ResponseEntity<?> getWishlistIds() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body("Please login");
            java.util.List<Long> ids = wishlistService.getWishlistForUser(user).stream().map(w -> w.getProduct().getId()).toList();
            return ResponseEntity.ok(ids);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/wishlist/api/merge")
    @ResponseBody
    public ResponseEntity<?> mergeWishlist(@RequestBody List<Long> productIds) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body("Please login");
            int added = wishlistService.mergeGuestWishlist(user, productIds);
            return ResponseEntity.ok(Map.of("success", true, "added", added));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
