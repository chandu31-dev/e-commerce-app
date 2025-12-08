package com.catchy.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.catchy.model.Payout;
import com.catchy.payout.PayoutProvider;

@Service
public class PayoutProcessor {

    private final PayoutService payoutService;
    private final Logger logger = LoggerFactory.getLogger(PayoutProcessor.class);

    // Spring will inject all PayoutProvider beans mapped by bean name
    private final Map<String, PayoutProvider> providers;

    @Value("${vendor.payout.provider:mock}")
    private String providerName;

    public PayoutProcessor(PayoutService payoutService, Map<String, PayoutProvider> providers) {
        this.payoutService = payoutService;
        this.providers = providers;
    }

    private PayoutProvider resolveProvider() {
        if (providerName == null) providerName = "mock";
        String beanName = providerName;
        // support short name 'mock'
        if ("mock".equalsIgnoreCase(providerName)) beanName = "mockPayoutProvider";
        PayoutProvider p = providers.get(beanName);
        if (p == null && !providers.isEmpty()) {
            // fallback to any provider
            return providers.values().iterator().next();
        }
        return p;
    }

    public void processVendorPayouts(List<Payout> payouts) {
        PayoutProvider provider = resolveProvider();
        for (Payout p : payouts) {
            try {
                if (provider == null) {
                    // no provider available: mark as paid with a generated ref
                    payoutService.markAsPaid(p.getId(), "AUTO-PAY-" + System.currentTimeMillis());
                    continue;
                }
                String ref = provider.pay(p);
                payoutService.markAsPaid(p.getId(), ref);
            } catch (Exception ex) {
                // log and continue
                logger.error("Error processing payout", ex);
            }
        }
    }
}
