package com.catchy;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.repository.CartItemRepository;
import com.catchy.repository.ProductRepository;
import com.catchy.repository.UserRepository;
import com.catchy.service.CartService;

@SpringBootTest
@ActiveProfiles("test")
public class CartMergeTests {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CartItemRepository cartItemRepository;

    @Test
    void mergeGuestCartAddsItemsRespectingStock() {
        User u = new User();
        u.setName("CartUser");
        u.setEmail("cartuser@example.com");
        u.setPassword("test123");
        u.setEnabled(true);
        userRepository.save(u);

        Product p = new Product();
        p.setName("CartProd");
        p.setPrice(new BigDecimal("299.99"));
        p.setStock(3);
        p.setCategory("Test");
        productRepository.save(p);

        var items = List.of(new com.catchy.dto.CartMergeItem(p.getId(), 2));
        int merged = cartService.mergeGuestCart(u, items);
        assertThat(merged).isEqualTo(1);

        // merging more than stock should cap quantity
        var items2 = List.of(new com.catchy.dto.CartMergeItem(p.getId(), 5));
        int merged2 = cartService.mergeGuestCart(u, items2);
        assertThat(merged2).isEqualTo(1);
    }
}
