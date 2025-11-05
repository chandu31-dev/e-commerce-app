package com.catchy.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.catchy.model.Order;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.OrderService;
import com.catchy.service.ProductService;
import com.catchy.service.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null || !currentUser.getRole().equals(User.Role.ADMIN)) {
                return "redirect:/";
            }
            List<Product> products = productService.getAllProducts();
            List<User> users = userService.getAllUsers();
            List<Order> orders = orderService.getAllOrders();
            model.addAttribute("products", products);
            model.addAttribute("users", users);
            model.addAttribute("orders", orders);
            model.addAttribute("categories", productService.getAllCategories());
            return "admin-dashboard";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createProduct(@RequestParam String name,
                                                              @RequestParam String description,
                                                              @RequestParam String category,
                                                              @RequestParam BigDecimal price,
                                                              @RequestParam String imageURL,
                                                              @RequestParam Integer stock) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = new Product(name, description, category, price, imageURL, stock);
            product = productService.createProduct(product);
            response.put("success", true);
            response.put("message", "Product created successfully");
            response.put("product", product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id,
                                                               @RequestParam(required = false) String name,
                                                               @RequestParam(required = false) String description,
                                                               @RequestParam(required = false) String category,
                                                               @RequestParam(required = false) BigDecimal price,
                                                               @RequestParam(required = false) String imageURL,
                                                               @RequestParam(required = false) Integer stock) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.updateProduct(id, name, description, category, price, imageURL, stock);
            response.put("success", true);
            response.put("message", "Product updated successfully");
            response.put("product", product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(id);
            response.put("success", true);
            response.put("message", "Product deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/api/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id,
                                                                   @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            com.catchy.model.Order.Status st = com.catchy.model.Order.Status.valueOf(status.toUpperCase());
            Order updated = orderService.updateOrderStatus(id, st);
            response.put("success", true);
            response.put("order", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException iae) {
            response.put("success", false);
            response.put("message", "Invalid status");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/api/products/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadProductImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Empty file");
                return ResponseEntity.ok(response);
            }

            String uploadsDir = "src/main/resources/static/uploads/";
            File dir = new File(uploadsDir);
            if (!dir.exists()) dir.mkdirs();

            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + "_" + original;
            Path target = Paths.get(uploadsDir).resolve(filename);
            file.transferTo(target.toFile());

            String imageUrl = "/uploads/" + filename;
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}

