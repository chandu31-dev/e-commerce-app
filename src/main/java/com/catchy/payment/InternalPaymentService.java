package com.catchy.payment;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.payment.dto.PaymentRequest;
import com.catchy.payment.dto.PaymentResponse;
import com.catchy.payment.provider.PaymentProvider;

@Service
public class InternalPaymentService {

    @Autowired
    private InternalPaymentRepository paymentRepository;

    @Autowired
    private PaymentProvider paymentProvider;

    @Autowired
    private PaymentAuditRepository paymentAuditRepository;

    @Value("${payment.upi.payee-vpa:merchant@upi}")
    private String payeeVpa;

    @Value("${payment.upi.payee-name:Catchy Store}")
    private String payeeName;

    @Autowired
    private com.catchy.service.OrderService orderService;

    @Autowired
    private com.catchy.repository.UserRepository userRepository;

    public PaymentResponse createPayment(PaymentRequest req) {
        PaymentLedger p = new PaymentLedger();
            p.setOrderId(req.getOrderId());
        p.setUserId(req.getUserId());
        p.setAmount(req.getAmount());
        p.setCurrency(req.getCurrency());
        p.setMethod(req.getMethod());
        // store optional metadata in notes as simple JSON so we can create order later
        try {
            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            if (req.getAddressId() != null) meta.put("addressId", req.getAddressId());
            if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) meta.put("couponCode", req.getCouponCode());
            if (!meta.isEmpty()) p.setNotes(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(meta));
        } catch (Exception ignored) {}

        p.setStatus(PaymentStatus.PENDING);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        p = paymentRepository.save(p);

        PaymentResponse provResp = paymentProvider.initiatePayment(p);

        p.setProviderPayload(provResp.getProviderData());
        p.setProviderReference(p.getProviderReference());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        provResp.setPaymentId(p.getId());
        return provResp;
    }

    @Transactional
    public PaymentResponse initiateMethod(Long paymentId, com.catchy.payment.PaymentMethod method) {
        PaymentLedger p = paymentRepository.findById(paymentId).orElse(null);
        PaymentResponse resp = new PaymentResponse();
        if (p == null) {
            resp.setMessage("Payment not found");
            return resp;
        }
        p.setMethod(method);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        PaymentResponse provResp = paymentProvider.initiatePayment(p);
        p.setProviderPayload(provResp.getProviderData());
        p.setProviderReference(p.getProviderReference());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        provResp.setPaymentId(p.getId());
        return provResp;
    }

    @Transactional
    public PaymentResponse settleCod(Long paymentId, String actor) {
        // mark success and attach providerTxnId for COD
        PaymentResponse resp = manualSettle(paymentId, "COD-PAID", actor);
        // attempt to complete order as payment is success
        try {
            completeOrderIfPaymentSuccess(paymentId);
        } catch (Exception ignored) {}
        return resp;
    }

    @Transactional
    public void completeOrderIfPaymentSuccess(Long paymentId) {
        PaymentLedger p = paymentRepository.findById(paymentId).orElse(null);
        if (p == null) return;
        if (p.getStatus() == null || !p.getStatus().name().equals("SUCCESS")) return;
        if (p.getOrderId() != null) return; // already associated

        // parse notes for address and coupon
        Long addressId = null;
        String couponCode = null;
        try {
            if (p.getNotes() != null && !p.getNotes().isBlank()) {
                var m = new com.fasterxml.jackson.databind.ObjectMapper().readValue(p.getNotes(), java.util.Map.class);
                if (m.containsKey("addressId")) addressId = m.get("addressId") == null ? null : Long.parseLong(String.valueOf(m.get("addressId")));
                if (m.containsKey("couponCode")) couponCode = (String) m.get("couponCode");
            }
        } catch (Exception ignored) {}

        // find user
        if (p.getUserId() == null) return;
        var userOpt = userRepository.findById(p.getUserId());
        if (userOpt.isEmpty()) return;
        var user = userOpt.get();

        // Place order using OrderService (this will clear cart and create order items)
        try {
            com.catchy.model.Order order = orderService.placeOrder(user, addressId, couponCode);
            p.setOrderId(order.getId());
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        } catch (Exception ex) {
            // If order placement fails, mark payment as FAILED and rethrow or log
            p.setStatus(PaymentStatus.FAILED);
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
            throw ex;
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(Long paymentId, String providerTxnId) {
        PaymentLedger p = paymentRepository.findById(paymentId).orElse(null);
            PaymentResponse resp = new PaymentResponse();
        if (p == null) {
            resp.setMessage("Payment not found");
            return resp;
        }
        PaymentResponse provResp = paymentProvider.verifyPayment(p, providerTxnId);
        p.setStatus(provResp.getStatus());
        p.setProviderPayload(p.getProviderPayload());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);
        return provResp;
    }

    @Transactional
    public PaymentResponse manualSettle(Long paymentId, String providerTxnId, String actor) {
        PaymentLedger p = paymentRepository.findById(paymentId).orElse(null);
        PaymentResponse resp = new PaymentResponse();
        if (p == null) {
            resp.setMessage("Payment not found");
            return resp;
        }
        // mark success and attach providerTxnId
        p.setProviderReference(providerTxnId);
        p.setStatus(PaymentStatus.SUCCESS);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        // audit
        try {
            PaymentAudit audit = new PaymentAudit();
            audit.setPaymentId(p.getId());
            audit.setAction("MANUAL_SETTLE");
            audit.setActor(actor == null ? "system" : actor);
            audit.setNotes("Manual settle by " + (actor == null ? "system" : actor));
            paymentAuditRepository.save(audit);
        } catch (Exception ignored) {}

        resp.setPaymentId(p.getId());
        resp.setStatus(p.getStatus());
        resp.setMessage("Manually settled");
        resp.setProviderData(providerTxnId);
        return resp;
    }

    public String generateUpiPayload(Long paymentId) {
        PaymentLedger p = paymentRepository.findById(paymentId).orElse(null);
        if (p == null) return null;
        // Build a UPI deep link: upi://pay?pa={vpa}&pn={name}&am=100.00&cu=INR&tn=Order%20123&tr=REF123
        try {
            String pa = this.payeeVpa;
            String pn = java.net.URLEncoder.encode(this.payeeName, "UTF-8");
            String am = p.getAmount().toPlainString();
            String tn = java.net.URLEncoder.encode("Order " + p.getOrderId(), "UTF-8");
            String tr = java.net.URLEncoder.encode("PAY-" + p.getId(), "UTF-8");
            String uri = String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=%s&tr=%s", pa, pn, am, tn, tr);
            return uri;
        } catch (Exception ex) {
            return null;
        }
    }

    public PaymentLedger find(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
}
