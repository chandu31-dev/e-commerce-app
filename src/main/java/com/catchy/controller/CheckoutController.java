package com.catchy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.catchy.service.OrderService;

@Controller
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        if (orderId != null) {
            try {
                var order = orderService.getOrderById(orderId);
                model.addAttribute("order", order);
            } catch (Exception e) {
                // ignore - view can handle missing order
            }
        }
        return "checkout";
    }
}
