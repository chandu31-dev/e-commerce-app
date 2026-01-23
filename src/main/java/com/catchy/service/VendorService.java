package com.catchy.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.model.Vendor;
import com.catchy.model.VendorProduct;
import com.catchy.repository.ProductRepository;
import com.catchy.repository.VendorProductRepository;
import com.catchy.repository.VendorRepository;

@Service
public class VendorService {
    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorProductRepository vendorProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired(required = false)
    private MailService mailService;

    @Transactional
    public Vendor registerVendor(User user, String shopName, String contactEmail, String description, String phoneNumber, String address,
                                 String companyName, String taxId, String kycDocumentUrl) {
        // Check if vendor already exists for this user
        if (vendorRepository.findByUser(user).isPresent()) {
            throw new RuntimeException("User is already a vendor");
        }

        // Check if shop name is unique
        if (vendorRepository.findByShopName(shopName).isPresent()) {
            throw new RuntimeException("Shop name already exists");
        }

        Vendor vendor = new Vendor(user, shopName, contactEmail);
        vendor.setDescription(description);
        vendor.setPhoneNumber(phoneNumber);
        vendor.setAddress(address);
        vendor.setCompanyName(companyName);
        vendor.setTaxId(taxId);
        vendor.setKycDocumentUrl(kycDocumentUrl);
        // No KYC/approval required -- mark vendor as approved immediately
        vendor.setKycStatus(Vendor.KycStatus.APPROVED);
        vendor.setApproved(true);

        return vendorRepository.save(vendor);
    }

    public Optional<Vendor> getVendorByUser(User user) {
        return vendorRepository.findByUser(user);
    }

    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    public List<Vendor> getAllApprovedVendors() {
        return vendorRepository.findByApproved(true);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    @Transactional
    public Vendor updateVendor(Long vendorId, String shopName, String description, String phoneNumber, String address) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (shopName != null && !shopName.isEmpty()) {
            vendor.setShopName(shopName);
        }
        if (description != null) {
            vendor.setDescription(description);
        }
        if (phoneNumber != null) {
            vendor.setPhoneNumber(phoneNumber);
        }
        if (address != null) {
            vendor.setAddress(address);
        }
        vendor.setUpdatedAt(LocalDateTime.now());

        return vendorRepository.save(vendor);
    }

    @Transactional
    public VendorProduct addProductToVendor(Vendor vendor, Long productId, BigDecimal vendorPrice, Integer stock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if vendor already sells this product
        if (vendorProductRepository.findByVendorAndProductId(vendor, productId).isPresent()) {
            throw new RuntimeException("Vendor already sells this product");
        }

        VendorProduct vendorProduct = new VendorProduct(vendor, product, vendorPrice, stock);
        VendorProduct saved = vendorProductRepository.save(vendorProduct);

        // Ensure the base Product stock reflects vendor listings (aggregate across vendors)
        try {
            int existing = product.getStock() == null ? 0 : product.getStock();
            int add = stock == null ? 0 : stock;
            product.setStock(existing + add);
            productRepository.save(product);
        } catch (Exception e) {
            // don't fail vendor creation if stock sync fails
        }

        try {
            if (mailService != null && vendor.getContactEmail() != null) {
                String subject = "New product listed: " + product.getName();
                String body = "Hi " + vendor.getShopName() + ",\n\n" +
                        "You have successfully listed a new product on Catchy:\n\n" +
                        "Product: " + product.getName() + "\n" +
                        "Product ID: " + product.getId() + "\n" +
                        "Vendor price: " + vendorPrice + "\n" +
                        "Stock: " + stock + "\n\n" +
                        "Thanks,\nCatchy Team";
                mailService.sendVendorNotificationEmail(vendor.getContactEmail(), subject, body);
            }
        } catch (Exception e) {
            // don't fail transaction on mail errors
        }

        return saved;
    }

    @Transactional
    public VendorProduct updateVendorProductStock(Long vendorProductId, Integer newStock) {
        VendorProduct vendorProduct = vendorProductRepository.findById(vendorProductId)
                .orElseThrow(() -> new RuntimeException("Vendor product not found"));

        Integer oldStock = vendorProduct.getStock();
        vendorProduct.setStock(newStock);
        vendorProduct.setUpdatedAt(LocalDateTime.now());

        VendorProduct saved = vendorProductRepository.save(vendorProduct);

        // Sync product stock by applying the delta between new and old vendor stock
        try {
            Product product = saved.getProduct();
            int pStock = product.getStock() == null ? 0 : product.getStock();
            int delta = (newStock == null ? 0 : newStock) - (oldStock == null ? 0 : oldStock);
            int updated = pStock + delta;
            if (updated < 0) updated = 0;
            product.setStock(updated);
            productRepository.save(product);
        } catch (Exception e) {
            // ignore sync errors
        }

        try {
            Vendor v = saved.getVendor();
            if (mailService != null && v != null && v.getContactEmail() != null) {
                String subject = "Stock updated for: " + saved.getProduct().getName();
                String body = "Hi " + v.getShopName() + ",\n\n" +
                        "The stock for your product has been updated.\n\n" +
                        "Product: " + saved.getProduct().getName() + "\n" +
                        "Product ID: " + saved.getProduct().getId() + "\n" +
                        "Previous stock: " + (oldStock == null ? "N/A" : oldStock) + "\n" +
                        "New stock: " + newStock + "\n\n" +
                        "Thanks,\nCatchy Team";
                mailService.sendVendorNotificationEmail(v.getContactEmail(), subject, body);
            }
        } catch (Exception e) {
            // ignore mail errors
        }

        return saved;
    }

    @Transactional
    public void removeProductFromVendor(Long vendorProductId) {
        // Before removing, decrement base product stock by vendor product stock
        var vp = vendorProductRepository.findById(vendorProductId).orElse(null);
        if (vp != null) {
            try {
                Product p = vp.getProduct();
                int pStock = p.getStock() == null ? 0 : p.getStock();
                int reduce = vp.getStock() == null ? 0 : vp.getStock();
                int updated = pStock - reduce;
                if (updated < 0) updated = 0;
                p.setStock(updated);
                productRepository.save(p);
            } catch (Exception e) {
                // ignore
            }
        }
        vendorProductRepository.deleteById(vendorProductId);
    }

    public List<VendorProduct> getVendorProducts(Vendor vendor) {
        return vendorProductRepository.findByVendor(vendor);
    }

    public List<VendorProduct> getVendorActiveProducts(Vendor vendor) {
        return vendorProductRepository.findByVendorAndActive(vendor, true);
    }

    @Transactional
    public Vendor approveVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setKycStatus(Vendor.KycStatus.APPROVED);
        vendor.setApproved(true);
        vendor.setUpdatedAt(java.time.LocalDateTime.now());
        return vendorRepository.save(vendor);
    }

    @Transactional
    public Vendor rejectVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setKycStatus(Vendor.KycStatus.REJECTED);
        vendor.setApproved(false);
        vendor.setUpdatedAt(java.time.LocalDateTime.now());
        return vendorRepository.save(vendor);
    }

    public List<Vendor> getVendorsByKycStatus(Vendor.KycStatus status) {
        return vendorRepository.findByKycStatus(status);
    }
}
