package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyVendorRedirectController {

    @GetMapping("/vendor-dashboard")
    public String redirectVendorDashboard() {
        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/vendor-products")
    public String redirectVendorProducts() {
        return "redirect:/vendor/products";
    }

    @GetMapping("/vendor-register")
    public String redirectVendorRegister() {
        return "redirect:/vendor/register";
    }
}
