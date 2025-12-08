package com.catchy.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.catchy.model.User;
import com.catchy.model.Vendor;
import com.catchy.model.VendorProduct;
import com.catchy.service.AuthService;
import com.catchy.service.ProductService;
import com.catchy.service.VendorService;

@Controller
@RequestMapping("/vendor")
public class VendorController {
    @Autowired
    private VendorService vendorService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    @Autowired(required = false)
    private com.catchy.service.MailService mailService;

    @Autowired(required = false)
    private com.catchy.util.JwtUtil jwtUtil;
    @GetMapping("/register")
    public String vendorRegisterPage(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                var opt = vendorService.getVendorByUser(currentUser);
                if (opt.isPresent()) {
                    // Already a vendor — forward to dashboard immediately
                    return "redirect:/vendor/dashboard";
                }
            }
        } catch (Exception e) {
            // Not logged in or not vendor
        }
        return "vendor-register";
    }

    @PostMapping("/api/register")
    @ResponseBody
        public ResponseEntity<Map<String, Object>> registerVendor(
            @RequestParam String shopName,
            @RequestParam String contactEmail,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) MultipartFile kycDocument) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            // If a Vendor record already exists for this user, block duplicate registration
            if (vendorService.getVendorByUser(user).isPresent()) {
                response.put("success", false);
                response.put("message", "You are already a vendor");
                return ResponseEntity.ok(response);
            }

            String kycUrl = null;
            if (kycDocument != null && !kycDocument.isEmpty()) {
                try {
                    String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "kyc";
                    File dir = new File(uploadsDir);
                    if (!dir.exists()) dir.mkdirs();
                    String filename = System.currentTimeMillis() + "_" + kycDocument.getOriginalFilename();
                    Path target = Path.of(uploadsDir, filename);
                    Files.copy(kycDocument.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                    kycUrl = "/uploads/kyc/" + filename;
                } catch (IOException ioe) {
                    // ignore file save error, continue without document
                }
            }

            Vendor vendor = vendorService.registerVendor(user, shopName, contactEmail, description, phoneNumber, address, companyName, taxId, kycUrl);

            // Immediately grant the VENDOR role and enable access — remove approval/validation gating
            try {
                user.setRole(com.catchy.model.User.Role.VENDOR);
                authService.saveUser(user);
            } catch (Exception ignore) {}

            // Notify vendor about registration (no admin approval required)
            try {
                if (mailService != null) {
                    String vendorBody = "Hi " + user.getName() + ",\n\nYour vendor registration is successful. You can access your vendor dashboard now.\n\nThanks.";
                    mailService.sendVendorNotificationEmail(contactEmail != null ? contactEmail : user.getEmail(), "Vendor registration successful", vendorBody);
                }
            } catch (Exception e) {
                // ignore email failures
            }

            response.put("success", true);
            response.put("message", "Vendor registration successful!");
            response.put("vendor", vendor);
            // Return new JWT token so client can update stored token and access vendor-only routes
            try {
                if (jwtUtil != null) {
                    String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
                    response.put("token", newToken);
                }
            } catch (Exception ignore) {}
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('VENDOR')")
    public String vendorDashboard(Model model) {
        try {
            User user = authService.getCurrentUser();
            Vendor vendor = vendorService.getVendorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<VendorProduct> products = vendorService.getVendorProducts(vendor);
            int totalProducts = products.size();
            int totalActive = (int) products.stream().filter(VendorProduct::isActive).count();

            model.addAttribute("vendor", vendor);
            model.addAttribute("products", products);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalActive", totalActive);
            model.addAttribute("totalOrders", vendor.getTotalOrders());
            model.addAttribute("totalSales", vendor.getTotalSales());

            return "vendor-dashboard";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('VENDOR')")
    public String vendorProducts(Model model) {
        try {
            User user = authService.getCurrentUser();
            Vendor vendor = vendorService.getVendorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<VendorProduct> products = vendorService.getVendorProducts(vendor);
            model.addAttribute("vendor", vendor);
            model.addAttribute("products", products);
            model.addAttribute("allProducts", productService.getAllProducts());

            return "vendor-products";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

        @PostMapping("/api/products/add")
    @PreAuthorize("hasRole('VENDOR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProductToVendor(
            @RequestParam(required = false) Long productId,
            @RequestParam BigDecimal vendorPrice,
            @RequestParam Integer stock,
            @RequestParam(required = false) String newName,
            @RequestParam(required = false) String newCategory,
            @RequestParam(required = false) String newPrice,
            @RequestParam(required = false) String newImage,
            @RequestParam(required = false) MultipartFile newImageFile,
            @RequestParam(required = false) String newDescription) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            Vendor vendor = vendorService.getVendorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            // If vendor wants to create a new base product on the fly
            if ((productId == null || productId == 0) && newName != null && !newName.isEmpty()) {
                // create a Product using ProductService
                com.catchy.model.Product product = new com.catchy.model.Product();
                product.setName(newName);
                if (newCategory != null && !newCategory.isEmpty()) product.setCategory(newCategory);
                else product.setCategory("General");
                if (newDescription != null && !newDescription.isEmpty()) product.setDescription(newDescription);
                String imageUrl = null;
                if (newImageFile != null && !newImageFile.isEmpty()) {
                    try {
                        String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "products";
                        File dir = new File(uploadsDir);
                        if (!dir.exists()) dir.mkdirs();
                        String filename = System.currentTimeMillis() + "_" + newImageFile.getOriginalFilename();
                        Path target = Path.of(uploadsDir, filename);
                        Files.copy(newImageFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                        imageUrl = "/uploads/products/" + filename;
                    } catch (IOException ioe) {
                        // ignore file save error, continue without image
                    }
                } else if (newImage != null && !newImage.isEmpty()) {
                    imageUrl = newImage;
                }
                if (imageUrl != null) product.setImageURL(imageUrl);
                if (newPrice != null && !newPrice.isEmpty()) {
                    try {
                        java.math.BigDecimal bp = new java.math.BigDecimal(newPrice);
                        product.setPrice(bp);
                    } catch (Exception ex) {
                        // ignore parse error
                    }
                }
                // If price still null or zero, use vendorPrice as base price so product passes validation
                if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    product.setPrice(vendorPrice != null ? vendorPrice : java.math.BigDecimal.valueOf(1));
                }
                // default stock - actual vendor stock will be in VendorProduct
                product.setStock(0);
                product = productService.createProduct(product);
                productId = product.getId();
            }

            VendorProduct vendorProduct = vendorService.addProductToVendor(vendor, productId, vendorPrice, stock);
            response.put("success", true);
            response.put("message", "Product added successfully!");
            response.put("vendorProduct", vendorProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/api/products/{id}/stock")
    @PreAuthorize("hasRole('VENDOR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.getCurrentUser();
            @SuppressWarnings("unused")
            var vendor = vendorService.getVendorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            VendorProduct vendorProduct = vendorService.updateVendorProductStock(id, stock);
            response.put("success", true);
            response.put("message", "Stock updated successfully!");
            response.put("vendorProduct", vendorProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/api/products/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            vendorService.removeProductFromVendor(id);
            response.put("success", true);
            response.put("message", "Product removed successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/api/profile")
    @PreAuthorize("hasRole('VENDOR')")
    @ResponseBody
    public ResponseEntity<Vendor> getVendorProfile() {
        try {
            User user = authService.getCurrentUser();
            Vendor vendor = vendorService.getVendorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            return ResponseEntity.ok(vendor);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
