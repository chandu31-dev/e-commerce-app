package com.catchy.wow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.catchy.model.Product;
import com.catchy.repository.ProductRepository;
import com.catchy.wow.dto.WowRecommendation;

@Service
public class WowService {
    private final ProductRepository productRepository;

    public WowService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<WowRecommendation> getTopRecommendations(int limit) {
        List<Product> all = productRepository.findAll();
        return all.stream()
                .limit(Math.max(0, limit))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "wowTop", key = "#limit")
    public List<WowRecommendation> getTopRecommendationsCached(int limit) {
        return getTopRecommendations(limit);
    }

    public List<WowRecommendation> getSimilarProducts(Long productId, int limit) {
        Optional<Product> pOpt = productRepository.findById(productId);
        if (pOpt.isEmpty()) {
            return List.of();
        }

        Product p = pOpt.get();
        List<Product> byCategory = productRepository.findByCategoryOrderByNameAsc(p.getCategory());
        return byCategory.stream()
                .filter(prod -> !prod.getId().equals(productId))
                .limit(Math.max(0, limit))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "wowSimilar", key = "#productId + ':' + #limit")
    public List<WowRecommendation> getSimilarProductsCached(Long productId, int limit) {
        return getSimilarProducts(productId, limit);
    }

    private WowRecommendation toDto(Product p) {
        return new WowRecommendation(p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getImageURL());
    }
}
