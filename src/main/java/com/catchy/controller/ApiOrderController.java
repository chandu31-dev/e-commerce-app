package com.catchy.controller;

import com.catchy.model.Order;
import com.catchy.service.OrderService;
import com.catchy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class ApiOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        try {
            // Access control: ensure current user can view the order
            var user = authService.getCurrentUser();
            Order order = orderService.getOrderById(id);
            if (order == null) return ResponseEntity.notFound().build();
            if (user == null) return ResponseEntity.status(401).build();
            if (!order.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.catchy.model.User.Role.ADMIN)) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
