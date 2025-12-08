package com.catchy.dto;

import java.util.List;

/**
 * Request payload used by the client after a successful social login
 * to merge guest wishlist and cart state into the authenticated user.
 */
public class SocialLoginCompleteRequest {
    public List<Long> wishlistIds;
    public List<CartMergeItem> cartItems;

    public SocialLoginCompleteRequest() {}

    public SocialLoginCompleteRequest(List<Long> wishlistIds, List<CartMergeItem> cartItems) {
        this.wishlistIds = wishlistIds;
        this.cartItems = cartItems;
    }
}
