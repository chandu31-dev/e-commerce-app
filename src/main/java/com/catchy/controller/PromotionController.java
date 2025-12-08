package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PromotionController {

    @GetMapping("/promotions")
    public String promotions() {
        return "promotions";
    }
}
