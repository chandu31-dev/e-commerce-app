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
        // Checkout page has been removed — redirect to payments landing
        return "redirect:/payments";
    }
}
