package com.catchy.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.Order;
import com.catchy.model.OrderItem;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public String ordersPage(Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            List<Order> orders = orderService.getUserOrders(user);
            model.addAttribute("orders", orders);
            return "orders";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            Order order = orderService.getOrderById(id);
            if (!order.getUser().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
                return "redirect:/orders";
            }
            List<OrderItem> orderItems = orderService.getOrderItems(order);
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            return "order-details";
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }

    @PostMapping("/api/place")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            Long addressId = null;
            if (body != null && body.containsKey("addressId")) {
                Object addressIdObj = body.get("addressId");
                if (addressIdObj instanceof Number) {
                    addressId = ((Number) addressIdObj).longValue();
                }
            }

            String couponCode = null;
            if (body != null && body.containsKey("couponCode")) {
                Object cc = body.get("couponCode");
                if (cc != null) couponCode = String.valueOf(cc);
            }

            Order order = orderService.placeOrder(user, addressId, couponCode);
            response.put("success", true);
            response.put("message", "Order placed successfully");
            response.put("orderId", order.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/api/my-orders")
    @ResponseBody
    public ResponseEntity<List<Order>> getMyOrders() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(orderService.getUserOrders(user));
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}

