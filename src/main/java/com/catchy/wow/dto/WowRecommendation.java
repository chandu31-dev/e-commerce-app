package com.catchy.wow.dto;

import java.math.BigDecimal;

public class WowRecommendation {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String imageURL;

    public WowRecommendation() {}

    public WowRecommendation(Long id, String name, String category, BigDecimal price, String imageURL) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.imageURL = imageURL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
