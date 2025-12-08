package com.catchy.dto;

import java.math.BigDecimal;

public class WishlistItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageURL;

    public WishlistItemDto() {}

    public WishlistItemDto(Long id, Long productId, String productName, BigDecimal price, String imageURL) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.imageURL = imageURL;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}
