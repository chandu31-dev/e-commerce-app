package com.catchy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private PaymentConfig paymentConfig;

    @ModelAttribute("currencySymbol")
    public String currencySymbol() {
        return paymentConfig.getCurrencySymbol();
    }

    @ModelAttribute("currencyCode")
    public String currencyCode() {
        return paymentConfig.getCurrencyCode();
    }
}
