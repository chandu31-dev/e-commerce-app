package com.catchy.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.dto.CartMergeItem;
import com.catchy.model.CartItem;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.repository.CartItemRepository;
import com.catchy.repository.ProductRepository;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    @SuppressWarnings("unused")
    private AuthService authService;

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    public BigDecimal getCartTotal(User user) {
        List<CartItem> cartItems = getCartItems(user);
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public CartItem addToCart(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product id=" + product.getId() + " name='" + product.getName() + "' stock=" + product.getStock() + " requested=" + quantity);
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProductId(user, productId);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Insufficient stock for product id=" + product.getId() + " name='" + product.getName() + "' stock=" + product.getStock() + " existing=" + cartItem.getQuantity() + " requestedAdd=" + quantity + " wouldBe=" + newQuantity);
            }
            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem(user, product, quantity);
            return cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public CartItem updateCartItemQuantity(User user, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        if (cartItem.getProduct().getStock() < quantity) {
            Product p = cartItem.getProduct();
            throw new RuntimeException("Insufficient stock for product id=" + p.getId() + " name='" + p.getName() + "' stock=" + p.getStock() + " requested=" + quantity);
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    @Transactional
    public int mergeGuestCart(User user, List<CartMergeItem> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) return 0;
        int merged = 0;
        for (CartMergeItem gi : guestItems) {
            if (gi == null || gi.productId == null || gi.quantity == null || gi.quantity <= 0) continue;
            try {
                Optional<CartItem> existing = cartItemRepository.findByUserAndProductId(user, gi.productId);
                Product product = productRepository.findById(gi.productId).orElse(null);
                if (product == null) continue;
                int qtyToAdd = Math.max(1, gi.quantity);
                if (existing.isPresent()) {
                    CartItem ci = existing.get();
                    int newQty = ci.getQuantity() + qtyToAdd;
                    if (product.getStock() < newQty) newQty = product.getStock();
                    ci.setQuantity(newQty);
                    cartItemRepository.save(ci);
                } else {
                    int finalQty = Math.min(product.getStock(), qtyToAdd);
                    if (finalQty <= 0) continue;
                    CartItem ci = new CartItem(user, product, finalQty);
                    cartItemRepository.save(ci);
                }
                merged++;
            } catch (Exception e) {
                // ignore individual failures
            }
        }
        return merged;
    }
}

