package com.catchy.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Product;
import com.catchy.repository.ProductRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public org.springframework.data.domain.Page<Product> searchProductsAdvanced(String q, String category, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Product> spec = com.catchy.spec.ProductSpecification.byFilters(q, category, minPrice, maxPrice);
        return productRepository.findAll(spec, pageable);
    }

    public List<String> suggestProductNames(String q, int limit) {
        if (q == null || q.isBlank()) return java.util.Collections.emptyList();
        String like = q.toLowerCase();
        return productRepository.findAll().stream()
                .map(Product::getName)
                .filter(name -> name != null && name.toLowerCase().contains(like))
                .distinct()
                .limit(limit)
                .toList();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public List<String> getAllCategories() {
        return productRepository.findAll().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, String name, String description, String category, 
                                  BigDecimal price, String imageURL, Integer stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (name != null && !name.isEmpty()) {
            product.setName(name);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (category != null && !category.isEmpty()) {
            product.setCategory(category);
        }
        if (price != null) {
            product.setPrice(price);
        }
        if (imageURL != null) {
            product.setImageURL(imageURL);
        }
        if (stock != null) {
            product.setStock(stock);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

