package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VendorDashboardController {

    @GetMapping("/legacy/vendor/dashboard")
    public String dashboard() {
        return "vendor-dashboard";
    }
}
