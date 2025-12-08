package com.catchy.payout;

import org.springframework.stereotype.Component;

import com.catchy.model.Payout;

@Component("mockPayoutProvider")
public class MockPayoutProvider implements PayoutProvider {

    @Override
    public String pay(Payout payout) throws Exception {
        // Simulate payment processing and return a fake reference
        return "MOCK-PAY-" + payout.getId() + "-" + System.currentTimeMillis();
    }
}
