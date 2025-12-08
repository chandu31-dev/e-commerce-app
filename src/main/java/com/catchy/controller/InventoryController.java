package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryController {

    @GetMapping("/vendor/inventory")
    public String inventory() {
        return "inventory";
    }
}
