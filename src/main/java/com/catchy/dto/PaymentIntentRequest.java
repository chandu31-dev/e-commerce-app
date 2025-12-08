package com.catchy.dto;

import java.math.BigDecimal;

// kept for compatibility; payments disabled

/**
 * Request DTO for creating payment intents in INR
 */
public class PaymentIntentRequest {
    
    private Long orderId;

    private BigDecimal amountInr;

    private String description;
    private String metadata;

    // Constructors
    public PaymentIntentRequest() {}

    public PaymentIntentRequest(Long orderId, BigDecimal amountInr) {
        this.orderId = orderId;
        this.amountInr = amountInr;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmountInr() {
        return amountInr;
    }

    public void setAmountInr(BigDecimal amountInr) {
        this.amountInr = amountInr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
