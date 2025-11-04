package com.catchy.controller;

import com.catchy.model.Order;
import com.catchy.model.OrderItem;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> placeOrder() {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }
            Order order = orderService.placeOrder(user);
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

