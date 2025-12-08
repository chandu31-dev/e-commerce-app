package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddressPageController {
    
    @GetMapping("/addresses")
    public String addressesPage() {
        return "addresses";
    }
}
