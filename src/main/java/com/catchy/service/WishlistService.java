package com.catchy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.dto.WishlistItemDto;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.model.WishlistItem;
import com.catchy.repository.WishlistRepository;

@Service
public class WishlistService {
    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductService productService;

    public List<WishlistItem> getWishlistForUser(User user) {
        return wishlistRepository.findByUser(user);
    }

    public Page<WishlistItemDto> getWishlistPage(User user, Pageable pageable) {
        Page<WishlistItem> page = wishlistRepository.findByUser(user, pageable);
        List<WishlistItemDto> dtos = page.stream().map(w -> new WishlistItemDto(
            w.getId(),
            w.getProduct().getId(),
            w.getProduct().getName(),
            w.getProduct().getPrice(),
            w.getProduct().getImageURL()
        )).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional
    public WishlistItem addToWishlist(User user, Product product) {
        WishlistItem item = new WishlistItem(user, product);
        return wishlistRepository.save(item);
    }

    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        wishlistRepository.deleteByUserAndProductId(user, productId);
    }

    @Transactional
    public int mergeGuestWishlist(User user, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return 0;
        int added = 0;
        for (Long pid : productIds) {
            if (pid == null) continue;
            try {
                if (wishlistRepository.existsByUserAndProductId(user, pid)) continue;
                java.util.Optional<Product> opt = productService.getProductById(pid);
                if (opt.isPresent()) {
                    wishlistRepository.save(new WishlistItem(user, opt.get()));
                    added++;
                }
            } catch (Exception e) {
                // ignore individual failures and continue
            }
        }
        return added;
    }
}
