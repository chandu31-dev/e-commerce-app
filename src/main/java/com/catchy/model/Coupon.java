package com.catchy.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double discountPercent;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal fixedAmount; // optional fixed discount amount

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "usage_limit")
    private Integer usageLimit; // null = unlimited

    @Column(name = "per_user_limit")
    private Integer perUserLimit; // null = unlimited per user

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private java.math.BigDecimal minOrderAmount;

    @Column(name = "applicable_categories", length = 500)
    private String applicableCategories; // comma-separated

    @Column(name = "applicable_product_ids", length = 1000)
    private String applicableProductIds; // comma-separated product ids

    @Column(name = "stackable")
    private boolean stackable = false;

    public Coupon() {}

    public Coupon(String code, Double discountPercent, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public java.math.BigDecimal getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(java.math.BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }

    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }

    public java.math.BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(java.math.BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public String getApplicableCategories() { return applicableCategories; }
    public void setApplicableCategories(String applicableCategories) { this.applicableCategories = applicableCategories; }

    public String getApplicableProductIds() { return applicableProductIds; }
    public void setApplicableProductIds(String applicableProductIds) { this.applicableProductIds = applicableProductIds; }

    public boolean isStackable() { return stackable; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
