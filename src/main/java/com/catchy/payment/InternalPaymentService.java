package com.catchy.payment;

import com.catchy.payment.dto.PaymentRequest;
import com.catchy.payment.dto.PaymentResponse;
import com.catchy.payment.provider.PaymentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Transactional
    public PaymentResponse createPayment(PaymentRequest req) {
        PaymentLedger p = new PaymentLedger();
            p.setOrderId(req.getOrderId());
        p.setUserId(req.getUserId());
        p.setAmount(req.getAmount());
        p.setCurrency(req.getCurrency());
        p.setMethod(req.getMethod());
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
