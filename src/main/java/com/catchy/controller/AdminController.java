package com.catchy.controller;

import com.catchy.model.Order;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.service.AuthService;
import com.catchy.service.OrderService;
import com.catchy.service.ProductService;
import com.catchy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private com.catchy.service.VendorService vendorService;
    @Autowired(required = false)
    private com.catchy.service.MailService mailService;

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

    @GetMapping("/vendors/kyc")
    public String kycReviewPage(Model model) {
        try {
            model.addAttribute("pendingVendors", vendorService.getVendorsByKycStatus(com.catchy.model.Vendor.KycStatus.PENDING));
            return "vendor-kyc-review";
        } catch (Exception e) {
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/api/vendors/{id}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveVendor(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            com.catchy.model.Vendor v = vendorService.approveVendor(id);
            // Ensure the linked user is granted VENDOR role upon approval
            try {
                com.catchy.model.User u = v.getUser();
                if (u != null && !u.getRole().equals(com.catchy.model.User.Role.VENDOR)) {
                    u.setRole(com.catchy.model.User.Role.VENDOR);
                    authService.saveUser(u);
                }
            } catch (Exception ex) {
                // ignore user save failures here; vendor is approved regardless
            }
            // notify vendor
            try {
                if (mailService != null && v.getContactEmail() != null) {
                    String body = "Hi " + v.getShopName() + ",\n\nYour vendor account has been approved. You can now list products and manage orders.\n\nThanks.";
                    mailService.sendVendorNotificationEmail(v.getContactEmail(), "Vendor account approved", body);
                }
            } catch (Exception ex) { }
            resp.put("success", true);
            resp.put("vendor", v);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(400).body(resp);
        }
    }

    @PostMapping("/api/vendors/{id}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectVendor(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            com.catchy.model.Vendor v = vendorService.rejectVendor(id);
            try {
                if (mailService != null && v.getContactEmail() != null) {
                    String body = "Hi " + v.getShopName() + ",\n\nYour vendor application has been rejected. Please contact support for details.\n\nThanks.";
                    mailService.sendVendorNotificationEmail(v.getContactEmail(), "Vendor application rejected", body);
                }
            } catch (Exception ex) { }
            resp.put("success", true);
            resp.put("vendor", v);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(400).body(resp);
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
}

