package com.catchy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.catchy.service.ReviewThrottleService;

public class ReviewThrottleTests {

    @Test
    void throttleBlocksWhenTooManyRequests() {
        ReviewThrottleService rts = new ReviewThrottleService();
        Long userId = 100L;
        Long productId = 200L;

        // first should pass
        rts.checkAllowed(userId, productId);

        // immediate second should be blocked by minSecondsPerProduct default (30s)
        assertThatThrownBy(() -> rts.checkAllowed(userId, productId)).isInstanceOf(RuntimeException.class);
    }
}
