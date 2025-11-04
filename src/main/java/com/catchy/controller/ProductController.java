package com.catchy.controller;

import com.catchy.model.Product;
import com.catchy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public String productsPage(@RequestParam(required = false) String category,
                               @RequestParam(required = false) String search,
                               Model model) {
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
            model.addAttribute("searchQuery", search);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.getAllProducts();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        return "products";
    }

    @GetMapping("/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "product-details";
    }

    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @GetMapping("/api/category/{category}")
    @ResponseBody
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }
}

