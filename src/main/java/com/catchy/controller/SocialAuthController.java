package com.catchy.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.dto.CartMergeItem;
import com.catchy.dto.SocialLoginCompleteRequest;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.CartService;
import com.catchy.service.WishlistService;

@Controller
public class SocialAuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CartService cartService;

    @GetMapping("/auth/social")
    public String socialLoginInfo() {
        return "social-auth";
    }

    @PostMapping("/auth/api/complete")
    @ResponseBody
    public ResponseEntity<?> completeSocialLogin(@RequestBody SocialLoginCompleteRequest req) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Please login"));

            int wishlistAdded = 0;
            int cartMerged = 0;

            if (req.wishlistIds != null && !req.wishlistIds.isEmpty()) {
                wishlistAdded = wishlistService.mergeGuestWishlist(user, req.wishlistIds);
            }

            if (req.cartItems != null && !req.cartItems.isEmpty()) {
                cartMerged = cartService.mergeGuestCart(user, (List<CartMergeItem>)(List<?>)req.cartItems);
            }

            return ResponseEntity.ok(Map.of("success", true, "wishlistAdded", wishlistAdded, "cartMerged", cartMerged));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
