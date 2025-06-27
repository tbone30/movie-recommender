package com.movierecommender.controller;

import com.movierecommender.dto.letterboxd.LetterboxdScrapeResponse;
import com.movierecommender.dto.letterboxd.LetterboxdProfile;
import com.movierecommender.service.LetterboxdIntegrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/letterboxd")
@CrossOrigin(origins = {"http://localhost:3000"})
public class LetterboxdController {
    
    private static final Logger logger = LoggerFactory.getLogger(LetterboxdController.class);
    
    private final LetterboxdIntegrationService letterboxdService;
    
    public LetterboxdController(LetterboxdIntegrationService letterboxdService) {
        this.letterboxdService = letterboxdService;
    }
    
    /**
     * Get service status and information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        try {
            Map<String, Object> status = letterboxdService.getScraperInfo();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting service status", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get service status"));
        }
    }
    
    /**
     * Validate if a Letterboxd user exists
     */
    @GetMapping("/user/{username}/validate")
    public ResponseEntity<Map<String, Object>> validateUser(@PathVariable String username) {
        try {
            boolean exists = letterboxdService.validateUser(username);
            return ResponseEntity.ok(Map.of(
                "username", username,
                "exists", exists,
                "message", exists ? "User exists on Letterboxd" : "User not found on Letterboxd"
            ));
        } catch (Exception e) {
            logger.error("Error validating user: {}", username, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to validate user"));
        }
    }
    
    /**
     * Get user profile information only
     */
    @GetMapping("/user/{username}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            logger.info("Fetching Letterboxd profile for user: {}", username);
            LetterboxdProfile profile = letterboxdService.getUserProfile(username);
            return ResponseEntity.ok(profile);
        } catch (LetterboxdIntegrationService.LetterboxdScrapingException e) {
            logger.error("Scraping error for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching profile for user: {}", username, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to fetch user profile"));
        }
    }
    
    /**
     * Scrape complete user data (ratings and watchlist)
     */
    @PostMapping("/user/{username}/scrape")
    public ResponseEntity<?> scrapeUserData(
            @PathVariable String username,
            @RequestParam(defaultValue = "true") boolean includeRatings,
            @RequestParam(defaultValue = "true") boolean includeWatchlist,
            @RequestParam(defaultValue = "100") int ratingLimit) {
        
        try {
            logger.info("Starting complete scrape for user: {} (ratings: {}, watchlist: {}, limit: {})", 
                       username, includeRatings, includeWatchlist, ratingLimit);
            
            LetterboxdScrapeResponse response = letterboxdService.scrapeUserData(
                username, includeRatings, includeWatchlist, ratingLimit
            );
            
            logger.info("Scrape completed for user: {} ({} ratings, {} watchlist items)", 
                       username, response.getTotalRatings(), response.getTotalWatchlistItems());
            
            return ResponseEntity.ok(response);
            
        } catch (LetterboxdIntegrationService.LetterboxdScrapingException e) {
            logger.error("Scraping error for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error scraping user: {}", username, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to scrape user data"));
        }
    }
    
    /**
     * Quick scrape with default settings
     */
    @PostMapping("/user/{username}/scrape/quick")
    public ResponseEntity<?> quickScrapeUserData(@PathVariable String username) {
        return scrapeUserData(username, true, true, 50);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean isHealthy = letterboxdService.isScraperHealthy();
            
            Map<String, Object> health = Map.of(
                "status", isHealthy ? "healthy" : "unhealthy",
                "service", "letterboxd-integration",
                "scraper_available", isHealthy
            );
            
            return isHealthy ? 
                ResponseEntity.ok(health) : 
                ResponseEntity.status(503).body(health);
                
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return ResponseEntity.status(503).body(Map.of(
                "status", "unhealthy",
                "error", e.getMessage()
            ));
        }
    }
}
