package com.catchy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.catchy.model.User;
import com.catchy.model.Vendor;
import com.catchy.model.VendorProduct;
import com.catchy.service.AuthService;
import com.catchy.service.VendorService;

@Controller
public class HomeController {

    @Autowired
    private AuthService authService;

    @Autowired
    private VendorService vendorService;

    @GetMapping("/buyer/home")
    public String buyerHome(Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole().equals(User.Role.VENDOR)) {
                return "redirect:/vendor/home";
            }
            return "buyer-home";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/buyer/home/view")
    public String buyerHomeView(Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            // Intentionally allow vendor users to view buyer home
            return "buyer-home";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/vendor/home")
    public String vendorHome(Model model) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            if (!user.getRole().equals(User.Role.VENDOR)) {
                return "redirect:/buyer/home";
            }
            
            // Check if vendor exists, if not redirect to registration
            var vendorOpt = vendorService.getVendorByUser(user);
            if (vendorOpt.isEmpty()) {
                return "redirect:/vendor/register";
            }
            
            Vendor vendor = vendorOpt.get();

            List<VendorProduct> products = vendorService.getVendorProducts(vendor);
            int totalProducts = products.size();
            int totalActive = (int) products.stream().filter(VendorProduct::isActive).count();

            model.addAttribute("vendor", vendor);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalActive", totalActive);
            model.addAttribute("totalOrders", vendor.getTotalOrders() != null ? vendor.getTotalOrders() : 0);
            model.addAttribute("totalSales", vendor.getTotalSales() != null ? vendor.getTotalSales() : 0);

            return "vendor-home";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }
}
