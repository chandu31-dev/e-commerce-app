package com.catchy.payment.provider;

import com.catchy.payment.PaymentLedger;
import com.catchy.payment.PaymentStatus;
import com.catchy.payment.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResponse initiatePayment(PaymentLedger payment) {
        String ref = "MOCK-" + UUID.randomUUID();
        payment.setProviderReference(ref);
        payment.setStatus(PaymentStatus.INITIATED);

        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentId(payment.getId());
        resp.setStatus(payment.getStatus());
        if (payment.getMethod() != null && payment.getMethod().name().equals("UPI")) {
            resp.setProviderData("upi://pay?pa=merchant@upi&pn=catchy&am=" + payment.getAmount());
        } else {
            resp.setProviderData("mock://pay?ref=" + ref);
        }
        resp.setMessage("Mock payment initiated");
        return resp;
    }

    @Override
    public PaymentResponse verifyPayment(PaymentLedger payment, String providerTxnId) {
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentId(payment.getId());
        if (providerTxnId != null && providerTxnId.startsWith("MOCK-SUCCESS")) {
            payment.setStatus(PaymentStatus.SUCCESS);
            resp.setStatus(PaymentStatus.SUCCESS);
            resp.setMessage("Mock payment verified as SUCCESS");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            resp.setStatus(PaymentStatus.FAILED);
            resp.setMessage("Mock payment marked as FAILED");
        }
        resp.setProviderData(providerTxnId);
        return resp;
    }
}
