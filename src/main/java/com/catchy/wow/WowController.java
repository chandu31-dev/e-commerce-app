package com.catchy.wow;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.wow.dto.WowRecommendation;

@RestController
@RequestMapping("/api/wow")
public class WowController {
    private final WowService wowService;

    public WowController(WowService wowService) {
        this.wowService = wowService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("wow-service: OK");
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<WowRecommendation>> recommendations(@RequestParam(defaultValue = "5") int limit) {
        List<WowRecommendation> list = wowService.getTopRecommendations(limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<WowRecommendation>> similar(@PathVariable Long productId,
                                                           @RequestParam(defaultValue = "5") int limit) {
        List<WowRecommendation> list = wowService.getSimilarProducts(productId, limit);
        return ResponseEntity.ok(list);
    }
}
