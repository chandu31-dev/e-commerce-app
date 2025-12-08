package com.catchy.dto;

public class CartMergeItem {
    public Long productId;
    public Integer quantity;

    public CartMergeItem() {}

    public CartMergeItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
