package com.catchy.wow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.catchy.model.Product;
import com.catchy.repository.ProductRepository;
import com.catchy.wow.dto.WowRecommendation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {WowService.class, WowCacheConfig.class})
public class WowServiceTest {

    @Autowired
    private WowService wowService;

    @MockBean
    private ProductRepository productRepository;

    @Test
    void topRecommendations_areCached() {
        Product p1 = new Product("A", "desc", "cat", new BigDecimal("10.00"), "img", 5);
        p1.setId(1L);
        Product p2 = new Product("B", "desc", "cat", new BigDecimal("20.00"), "img", 3);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<WowRecommendation> first = wowService.getTopRecommendationsCached(2);
        List<WowRecommendation> second = wowService.getTopRecommendationsCached(2);

        assertThat(first).hasSize(2);
        assertThat(second).hasSize(2);

        // repository.findAll should be called only once due to caching
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void similarProducts_areCached() {
        Product p1 = new Product("A", "desc", "shoes", new BigDecimal("30.00"), "img", 5);
        p1.setId(10L);
        Product p2 = new Product("B", "desc", "shoes", new BigDecimal("40.00"), "img", 2);
        p2.setId(11L);

        when(productRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(productRepository.findByCategoryOrderByNameAsc("shoes")).thenReturn(List.of(p1, p2));

        List<WowRecommendation> first = wowService.getSimilarProductsCached(10L, 2);
        List<WowRecommendation> second = wowService.getSimilarProductsCached(10L, 2);

        assertThat(first).hasSize(1); // excludes same product
        assertThat(second).hasSize(1);

        verify(productRepository, times(1)).findById(10L);
        verify(productRepository, times(1)).findByCategoryOrderByNameAsc("shoes");
    }
}
