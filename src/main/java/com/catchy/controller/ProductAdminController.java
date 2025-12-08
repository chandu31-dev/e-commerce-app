package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class ProductAdminController {

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageProducts() {
        return "product-manage";
    }
}
