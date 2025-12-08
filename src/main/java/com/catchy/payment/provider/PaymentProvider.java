package com.catchy.payment.provider;

import com.catchy.payment.PaymentLedger;
import com.catchy.payment.dto.PaymentResponse;

public interface PaymentProvider {
    PaymentResponse initiatePayment(PaymentLedger payment);

    PaymentResponse verifyPayment(PaymentLedger payment, String providerTxnId);
}
