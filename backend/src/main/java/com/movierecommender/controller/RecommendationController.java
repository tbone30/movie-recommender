package com.movierecommender.controller;

import com.movierecommender.entity.Recommendation;
import com.movierecommender.service.RecommendationService;
import com.movierecommender.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/recommendations")
@PreAuthorize("hasRole('USER')")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateRecommendations(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userPrincipal.getId());
            return ResponseEntity.ok(Map.of(
                "message", "Recommendations generated successfully",
                "count", recommendations.size(),
                "recommendations", recommendations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cold-start")
    public ResponseEntity<?> generateColdStartRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody(required = false) Map<String, List<String>> requestBody) {
        try {
            List<String> preferredGenres = null;
            if (requestBody != null) {
                preferredGenres = requestBody.get("preferred_genres");
            }
            
            List<Recommendation> recommendations = recommendationService.generateColdStartRecommendations(
                userPrincipal.getId(), preferredGenres);
            
            return ResponseEntity.ok(Map.of(
                "message", "Cold start recommendations generated successfully",
                "count", recommendations.size(),
                "recommendations", recommendations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Recommendation>> getUserRecommendations(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Recommendation> recommendations = recommendationService.getRecommendationsForUser(userPrincipal.getId());
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/algorithm/{algorithm}")
    public ResponseEntity<List<Recommendation>> getRecommendationsByAlgorithm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String algorithm) {
        List<Recommendation> recommendations = recommendationService.getRecommendationsByAlgorithm(
            userPrincipal.getId(), algorithm);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Recommendation>> getRecommendationsPaged(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Recommendation> recommendations = recommendationService.getRecommendationsByScore(
            userPrincipal.getId(), pageable);
        
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/{id}/viewed")
    public ResponseEntity<?> markAsViewed(@PathVariable Long id) {
        try {
            recommendationService.markRecommendationAsViewed(id);
            return ResponseEntity.ok(Map.of("message", "Recommendation marked as viewed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/clicked")
    public ResponseEntity<?> markAsClicked(@PathVariable Long id) {
        try {
            recommendationService.markRecommendationAsClicked(id);
            return ResponseEntity.ok(Map.of("message", "Recommendation marked as clicked"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<?> hideRecommendation(@PathVariable Long id) {
        try {
            recommendationService.hideRecommendation(id);
            return ResponseEntity.ok(Map.of("message", "Recommendation hidden"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getRecommendationCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        long count = recommendationService.getRecommendationCount(userPrincipal.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}