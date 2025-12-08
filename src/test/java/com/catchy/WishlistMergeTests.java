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
import com.catchy.repository.ProductRepository;
import com.catchy.repository.UserRepository;
import com.catchy.service.WishlistService;

@SpringBootTest
@ActiveProfiles("test")
public class WishlistMergeTests {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void mergeAddsNewItemsAndSkipsDuplicates() {
        User u = new User();
        u.setName("Test");
        u.setEmail("wmtest@example.com");
        u.setPassword("test123");
        u.setEnabled(true);
        userRepository.save(u);

        Product p1 = new Product();
        p1.setName("P1");
        p1.setPrice(new BigDecimal("499.99"));
        p1.setCategory("Test");
        p1.setStock(10);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("P2");
        p2.setPrice(new BigDecimal("999.99"));
        p2.setCategory("Test");
        p2.setStock(10);
        productRepository.save(p2);

        // initial merge
        int added = wishlistService.mergeGuestWishlist(u, List.of(p1.getId(), p2.getId()));
        assertThat(added).isEqualTo(2);

        // merge again: should add zero because duplicates
        int added2 = wishlistService.mergeGuestWishlist(u, List.of(p1.getId(), p2.getId()));
        assertThat(added2).isEqualTo(0);
    }
}
