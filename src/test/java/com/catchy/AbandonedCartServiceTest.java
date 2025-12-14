package com.catchy;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.catchy.model.CartItem;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.repository.AbandonedCartReminderRepository;
import com.catchy.repository.ProductRepository;
import com.catchy.repository.UserRepository;
import com.catchy.service.AbandonedCartService;
import com.catchy.service.CartService;
import com.catchy.service.MailService;

@SpringBootTest(properties = { "abandoned.cart.threshold.hours=0", "abandoned.cart.check-rate-ms=3600000" })
@ActiveProfiles("test")
public class AbandonedCartServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AbandonedCartServiceTest.class);

    @Autowired
    private AbandonedCartService abandonedCartService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AbandonedCartReminderRepository reminderRepository;
    @Autowired
    private com.catchy.repository.CartItemRepository cartItemRepository;

    @MockBean
    private MailService mailService;

    @Test
    public void sendsReminderWhenCartOld() {
        log.info("TEST START");
        User u = new User();
        u.setName("Abandon");
        u.setEmail("abandon@example.com");
        u.setPassword("pwd123");
        u.setEnabled(true);
        userRepository.save(u);

        Product p = new Product("ProdA","desc","cat",new java.math.BigDecimal("10.00"),"img", 5);
        productRepository.save(p);

        CartItem ci = cartService.addToCart(u, p.getId(), 1);
        log.info("Created cart item: {}", ci);
        
        // make the cart appear old
        ci.setUpdatedAt(LocalDateTime.now().minusDays(2));
        cartItemRepository.save(ci);
        log.info("Updated cart item updatedAt to {} days ago", 2);

        // sanity check: ensure the repository query finds the user
        var users = cartItemRepository.findDistinctUsersWithCartUpdatedBefore(LocalDateTime.now());
        log.info("Found {} users with old carts", users.size());
        assertThat(users).isNotEmpty();

        log.info("Calling processAbandonedCarts...");
        abandonedCartService.processAbandonedCarts();
        log.info("processAbandonedCarts completed");

        var r = reminderRepository.findTopByUserOrderByCreatedAtDesc(u);
        var all = reminderRepository.findAll();
        log.info("Reminders in repository: {}", all);
        assertThat(all).withFailMessage("No reminders found; reminders=%s", all).isNotEmpty();
        assertThat(r).isPresent();
        assertThat(r.get().getSentAt()).isNotNull();
    }
}
