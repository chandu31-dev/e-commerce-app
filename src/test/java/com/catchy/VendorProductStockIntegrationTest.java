package com.catchy;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.catchy.model.CartItem;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.model.Vendor;
import com.catchy.model.VendorProduct;
import com.catchy.repository.ProductRepository;
import com.catchy.repository.UserRepository;
import com.catchy.service.CartService;
import com.catchy.service.VendorService;

@SpringBootTest
@ActiveProfiles("test")
public class VendorProductStockIntegrationTest {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Test
    public void vendorStockAllowsBuyerToAddToCart() {
        // create vendor user and vendor
        User vendorUser = new User();
        vendorUser.setName("VendorUser");
        vendorUser.setEmail("vendor@example.com");
        vendorUser.setPassword("pwd123");
        vendorUser.setEnabled(true);
        userRepository.save(vendorUser);

        Vendor v = vendorService.registerVendor(vendorUser, "ShopX", "vendor@example.com", null, null, null, null, null, null);

        // create base product with zero stock
        Product product = productRepository.save(new Product("Widget","desc","general", new BigDecimal("10.00"), "img", 0));

        // vendor lists product with stock 5
        VendorProduct vp = vendorService.addProductToVendor(v, product.getId(), new BigDecimal("8.00"), 5);

        Product refreshed = productRepository.findById(product.getId()).get();
        assertThat(refreshed.getStock()).isEqualTo(5);

        // buyer adds to cart within stock
        User buyer = new User();
        buyer.setName("Buyer");
        buyer.setEmail("buyer@example.com");
        buyer.setPassword("pwd123");
        buyer.setEnabled(true);
        userRepository.save(buyer);

        CartItem ci = cartService.addToCart(buyer, product.getId(), 3);
        assertThat(ci.getQuantity()).isEqualTo(3);

        // adding more than remaining should fail
        Exception ex = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(buyer, product.getId(), 3);
        });
        assertThat(ex.getMessage()).contains("Insufficient stock");

        // reduce vendor stock to 2 and ensure cart update fails
        vendorService.updateVendorProductStock(vp.getId(), 2);
        Product after = productRepository.findById(product.getId()).get();
        assertThat(after.getStock()).isEqualTo(2);

        Exception ex2 = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItemQuantity(buyer, ci.getId(), 3);
        });
        assertThat(ex2.getMessage()).contains("Insufficient stock");
    }
}
