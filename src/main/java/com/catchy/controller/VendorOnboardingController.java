package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VendorOnboardingController {

    @GetMapping("/vendor/register-info")
    public String vendorRegisterInfo() {
        return "vendor-register";
    }
}
