package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PwaController {

    @GetMapping("/pwa-info")
    public String pwaInfo() {
        return "pwa-info";
    }
}
