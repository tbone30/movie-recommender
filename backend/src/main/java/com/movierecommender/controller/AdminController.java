package com.movierecommender.controller;

import com.movierecommender.entity.DatasetMetrics;
import com.movierecommender.entity.User;
import com.movierecommender.service.DatasetService;
import com.movierecommender.service.UserService;
import com.movierecommender.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            DatasetMetrics metrics = datasetService.getDatasetMetrics();
            
            Map<String, Object> dashboard = Map.of(
                "dataset_metrics", metrics,
                "total_users", userService.getTotalUserCount(),
                "active_users", userService.getActiveUserCount(),
                "system_status", "operational"
            );
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok(Map.of("message", "User activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/dataset/rebuild")
    public ResponseEntity<?> rebuildDataset() {
        try {
            datasetService.rebuildDataset();
            return ResponseEntity.ok(Map.of("message", "Dataset rebuild initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/ml/retrain")
    public ResponseEntity<?> retrainModels() {
        try {
            datasetService.triggerModelTraining();
            return ResponseEntity.ok(Map.of("message", "Model retraining initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ml/status")
    public ResponseEntity<Map<String, Object>> getMLStatus() {
        try {
            Map<String, Object> status = datasetService.getMLServiceStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/data/collect")
    public ResponseEntity<?> triggerDataCollection(@RequestBody Map<String, List<String>> requestBody) {
        try {
            List<String> usernames = requestBody.get("usernames");
            if (usernames == null || usernames.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usernames list is required"));
            }

            datasetService.triggerBulkDataCollection(usernames);
            return ResponseEntity.ok(Map.of("message", "Bulk data collection initiated", "count", usernames.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/recommendations/regenerate-all")
    public ResponseEntity<?> regenerateAllRecommendations() {
        try {
            recommendationService.regenerateAllUserRecommendations();
            return ResponseEntity.ok(Map.of("message", "Recommendations regeneration initiated for all users"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/system/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = Map.of(
                "dataset_health", datasetService.getDatasetHealth(),
                "recommendation_accuracy", recommendationService.getRecommendationAccuracy(),
                "system_performance", getSystemPerformanceMetrics()
            );
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/system/cleanup")
    public ResponseEntity<?> performSystemCleanup() {
        try {
            datasetService.performSystemCleanup();
            return ResponseEntity.ok(Map.of("message", "System cleanup completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> getSystemPerformanceMetrics() {
        // In a real implementation, this would collect actual system metrics
        return Map.of(
            "memory_usage", "75%",
            "cpu_usage", "45%",
            "database_connections", 25,
            "active_sessions", 150
        );
    }
}