package com.catchy.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.catchy.dto.PaymentResponse;
import com.catchy.model.Order;
import com.catchy.payment.InternalPaymentService;
import com.catchy.payment.PaymentMethod;
import com.catchy.payment.dto.PaymentRequest;

/**
 * Payments are disabled in this build. This stub replaces the Stripe-backed implementation
 * to avoid compile/runtime dependency on the Stripe SDK.
 */
@Service
@ConditionalOnProperty(prefix = "features", name = "addresses-only", havingValue = "false", matchIfMissing = false)
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired(required = false)
    private InternalPaymentService internalPaymentService;

    public PaymentResponse createPaymentIntentForOrder(Order order, BigDecimal amountInr) {
        if (internalPaymentService == null) {
            logger.warn("createPaymentIntentForOrder called but internal payment service not available - returning stubbed intent");
            PaymentResponse resp = new PaymentResponse(true, "Payment intent created (stub)");
            resp.setPaymentId(-1L);
            resp.setOrderId(order == null ? null : order.getId());
            resp.setAmount(amountInr == null ? null : amountInr);
            resp.setCurrency("INR");
            resp.setCurrencySymbol("₹");
            resp.setClientSecret("pi_stub_client_secret");
            resp.setStatus("REQUIRES_CONFIRMATION");
            resp.setSuccess(true);
            return resp;
        }

        try {
            PaymentRequest req = new PaymentRequest();
            req.setOrderId(order.getId());
            req.setUserId(order.getUser() == null ? null : order.getUser().getId());
            req.setAmount(amountInr);
            req.setCurrency("INR");
            req.setMethod(PaymentMethod.UPI);

            com.catchy.payment.dto.PaymentResponse ipr = internalPaymentService.createPayment(req);

            PaymentResponse resp = new PaymentResponse(true, "Payment initiated");
            resp.setPaymentId(ipr.getPaymentId());
            resp.setOrderId(order.getId());
            resp.setStatus(ipr.getStatus() == null ? null : ipr.getStatus().name());
            resp.setAmount(amountInr);
            resp.setCurrency("INR");
            resp.setCurrencySymbol("₹");
            resp.setPaymentMethod(req.getMethod() == null ? null : req.getMethod().name());
            resp.setTransactionId(ipr.getProviderData());
            resp.setClientSecret(ipr.getProviderData());
            resp.setCreatedAt(LocalDateTime.now());
            resp.setUpdatedAt(LocalDateTime.now());
            resp.setSuccess(true);
            resp.setMessage(ipr.getMessage());
            return resp;
        } catch (Exception ex) {
            logger.error("Error creating payment intent", ex);
            return new PaymentResponse(false, "Failed to create payment intent: " + ex.getMessage());
        }
    }

    public PaymentResponse confirmPaymentIntent(String paymentIntentId) {
        if (internalPaymentService == null) {
            logger.warn("confirmPaymentIntent called but internal payment service not available - returning stubbed confirmation");
            PaymentResponse resp = new PaymentResponse(true, "Payment confirmed (stub)");
            resp.setPaymentId(null);
            resp.setStatus("SUCCEEDED");
            resp.setSuccess(true);
            return resp;
        }
        // otherwise fallthrough to real provider if available
        logger.warn("confirmPaymentIntent forwarding to internal provider");
        return new PaymentResponse(false, "Payments are disabled on this build");
    }

    public PaymentResponse getPaymentResponseByOrderId(Long orderId) {
        return new PaymentResponse(false, "Payments are disabled on this build");
    }

    public Object getPaymentByOrderId(Long orderId) {
        return null;
    }

    public Map<String, String> getPublicPaymentConfig() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("publishableKey", "");
        cfg.put("currency", "inr");
        cfg.put("currencySymbol", "₹");
        cfg.put("currencyCode", "INR");
        return cfg;
    }

    public PaymentResponse refundPayment(String paymentIntentId) {
        logger.warn("refundPayment called but payments are disabled");
        return new PaymentResponse(false, "Payments are disabled on this build");
    }
}
