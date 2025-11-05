package com.catchy.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public String cartPage(Model model) {
        try {
            User user = authService.getCurrentUser();
            List<CartItem> cartItems;
            BigDecimal total;
            if (user == null) {
                return "redirect:/login";
            } else {
                cartItems = cartService.getCartItems(user);
                total = cartService.getCartTotal(user);
            }
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
                                                          @RequestParam(defaultValue = "1") Integer quantity,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                // Guest: use/create session cookie
                String sessionId = null;
                if (request.getCookies() != null) {
                    for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                        if ("CART_SESSION".equals(cookie.getName())) {
                            sessionId = cookie.getValue();
                            break;
                        }
                    }
                }
                if (sessionId == null) {
                    sessionId = UUID.randomUUID().toString();
                    Cookie cookie = new Cookie("CART_SESSION", sessionId);
                    cookie.setPath("/");
                    cookie.setHttpOnly(false);
                    response.addCookie(cookie);
                }

                cartService.addToCartBySession(sessionId, productId, quantity);
            } else {
                cartService.addToCart(user, productId, quantity);
            }
            result.put("success", true);
            result.put("message", "Product added to cart");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(@PathVariable Long id, 
                                                               @RequestParam Integer quantity,
                                                               HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                String sessionId = null;
                if (request.getCookies() != null) {
                    for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                        if ("CART_SESSION".equals(cookie.getName())) {
                            sessionId = cookie.getValue();
                            break;
                        }
                    }
                }
                if (sessionId == null) {
                    response.put("success", false);
                    response.put("message", "No cart session");
                    return ResponseEntity.ok(response);
                }
                cartService.updateCartItemQuantityBySession(sessionId, id, quantity);
            } else {
                cartService.updateCartItemQuantity(user, id, quantity);
            }
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
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                String sessionId = null;
                if (request.getCookies() != null) {
                    for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                        if ("CART_SESSION".equals(cookie.getName())) {
                            sessionId = cookie.getValue();
                            break;
                        }
                    }
                }
                if (sessionId == null) {
                    response.put("success", false);
                    response.put("message", "No cart session");
                    return ResponseEntity.ok(response);
                }
                cartService.removeFromCartBySession(sessionId, id);
            } else {
                cartService.removeFromCart(user, id);
            }
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
            if (user != null) {
                return ResponseEntity.ok(cartService.getCartItems(user));
            }

            // Guest: return session cart
            String sessionId = null;
            HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                    if ("CART_SESSION".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                        break;
                    }
                }
            }
            if (sessionId == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(cartService.getCartItemsBySession(sessionId));
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}

