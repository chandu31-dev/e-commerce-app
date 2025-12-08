package com.catchy.scheduler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.catchy.model.Payout;
import com.catchy.service.PayoutService;

@Component
public class PayoutScheduler {

    private final PayoutService payoutService;

    public PayoutScheduler(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    // Runs daily by default; configurable via property 'vendor.payout.cron'
    @Scheduled(cron = "${vendor.payout.cron:0 0 0 * * *}")
    public void processPendingPayouts() {
        int minDays = Integer.parseInt(System.getProperty("vendor.payout.min-days", "7"));
        try {
            java.util.List<Payout> pending = payoutService.findPendingOlderThanDays(minDays);
            if (pending == null || pending.isEmpty()) return;

            // group by vendor id
            Map<Long, List<Payout>> byVendor = pending.stream().collect(Collectors.groupingBy(p -> p.getVendor().getId()));

            String batchRef = "BATCH-" + java.time.LocalDateTime.now().toString();

            // Delegate actual payment processing to PayoutProcessor
            org.springframework.context.ApplicationContext ctx = null;
            try {
                // get PayoutProcessor bean from Spring
                ctx = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
            } catch (Exception ignore) {}

            com.catchy.service.PayoutProcessor processor = null;
            if (ctx != null && ctx.containsBean("payoutProcessor")) {
                processor = ctx.getBean(com.catchy.service.PayoutProcessor.class);
            }

            for (Map.Entry<Long, List<Payout>> e : byVendor.entrySet()) {
                List<Payout> vendorPayouts = e.getValue();
                if (processor != null) {
                    processor.processVendorPayouts(vendorPayouts);
                } else {
                    // fallback: mark as paid
                    payoutService.markPayoutsAsPaidBatch(vendorPayouts, batchRef);
                }
            }
        } catch (Exception ex) {
            // log and continue; don't throw
            ex.printStackTrace();
        }
    }
}
