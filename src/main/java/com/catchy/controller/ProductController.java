package com.catchy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.catchy.model.Product;
import com.catchy.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private com.catchy.service.ReviewService reviewService;
    @Autowired
    private com.catchy.service.CouponService couponService;

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
    public String productDetails(@PathVariable Long id, Model model, @RequestParam(defaultValue = "0") int reviewPage,
                                 @RequestParam(defaultValue = "5") int reviewSize) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        // load paginated reviews for product
        org.springframework.data.domain.Page<com.catchy.model.Review> reviewsPage = reviewService.getReviewsForProductPage(product, org.springframework.data.domain.PageRequest.of(reviewPage, reviewSize, org.springframework.data.domain.Sort.by("createdAt").descending()));
        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("reviews", reviewsPage.getContent());
        model.addAttribute("reviewPageNumber", reviewsPage.getNumber());
        model.addAttribute("reviewTotalPages", reviewsPage.getTotalPages());
        model.addAttribute("averageRating", reviewService.getAverageRating(product));
        // load active coupons (filter by active and valid date)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.List<com.catchy.model.Coupon> coupons = couponService.getAllCoupons().stream()
            .filter(c -> c.isActive() && (c.getValidFrom().isBefore(now) || c.getValidFrom().isEqual(now))
                && (c.getValidUntil().isAfter(now) || c.getValidUntil().isEqual(now)))
            .toList();
        model.addAttribute("coupons", coupons);
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

    @GetMapping("/api/search-advanced")
    @ResponseBody
    public ResponseEntity<org.springframework.data.domain.Page<Product>> searchAdvanced(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "price,asc") String sort
    ) {
        String[] sortParts = sort.split(",");
        org.springframework.data.domain.Sort s = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortParts.length>1?sortParts[1]:"asc"), sortParts[0]);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, s);
        org.springframework.data.domain.Page<Product> result = productService.searchProductsAdvanced(q, category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/suggest")
    @ResponseBody
    public ResponseEntity<List<String>> suggest(@RequestParam String q, @RequestParam(required = false, defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.suggestProductNames(q, limit));
    }

    @GetMapping("/api/category/{category}")
    @ResponseBody
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }
}

