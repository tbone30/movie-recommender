package com.movierecommender.service;

import com.movierecommender.config.LetterboxdScraperConfig;
import com.movierecommender.dto.letterboxd.LetterboxdScrapeRequest;
import com.movierecommender.dto.letterboxd.LetterboxdScrapeResponse;
import com.movierecommender.dto.letterboxd.LetterboxdProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class LetterboxdIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LetterboxdIntegrationService.class);
    
    private final RestTemplate restTemplate;
    private final LetterboxdScraperConfig.LetterboxdScraperProperties properties;
    
    public LetterboxdIntegrationService(RestTemplate restTemplate, 
                                       LetterboxdScraperConfig.LetterboxdScraperProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }
    
    /**
     * Scrape complete user data from Letterboxd
     */
    public LetterboxdScrapeResponse scrapeUserData(String username) {
        return scrapeUserData(username, true, true, 100);
    }
    
    /**
     * Scrape user data with custom options
     */
    public LetterboxdScrapeResponse scrapeUserData(String username, boolean includeRatings, 
                                                  boolean includeWatchlist, int ratingLimit) {
        if (!properties.isEnabled()) {
            logger.warn("Letterboxd scraper is disabled");
            throw new LetterboxdScrapingException("Letterboxd scraper service is disabled");
        }
        
        try {
            LetterboxdScrapeRequest request = new LetterboxdScrapeRequest(username);
            request.setIncludeRatings(includeRatings);
            request.setIncludeWatchlist(includeWatchlist);
            request.setRatingLimit(ratingLimit);
            
            String url = properties.getBaseUrl() + "/api/scrape/user";
            
            logger.info("Scraping Letterboxd data for user: {}", username);
            
            LetterboxdScrapeResponse response = restTemplate.postForObject(
                url, request, LetterboxdScrapeResponse.class
            );
            
            if (response != null && response.isSuccess()) {
                logger.info("Successfully scraped data for user: {} ({} ratings, {} watchlist items)", 
                           username, response.getTotalRatings(), response.getTotalWatchlistItems());
                return response;
            } else {
                String errorMessage = response != null ? response.getErrorMessage() : "Unknown error";
                logger.error("Scraping failed for user {}: {}", username, errorMessage);
                throw new LetterboxdScrapingException("Scraping failed: " + errorMessage);
            }
            
        } catch (RestClientException e) {
            logger.error("Network error scraping Letterboxd data for user: {}", username, e);
            throw new LetterboxdScrapingException("Network error during scraping", e);
        } catch (Exception e) {
            logger.error("Unexpected error scraping Letterboxd data for user: {}", username, e);
            throw new LetterboxdScrapingException("Unexpected error during scraping", e);
        }
    }
    
    /**
     * Get only user profile information
     */
    public LetterboxdProfile getUserProfile(String username) {
        if (!properties.isEnabled()) {
            logger.warn("Letterboxd scraper is disabled");
            throw new LetterboxdScrapingException("Letterboxd scraper service is disabled");
        }
        
        try {
            String url = properties.getBaseUrl() + "/api/user/" + username + "/profile";
            
            logger.info("Fetching Letterboxd profile for user: {}", username);
            
            LetterboxdProfile profile = restTemplate.getForObject(url, LetterboxdProfile.class);
            
            if (profile != null) {
                logger.info("Successfully fetched profile for user: {}", username);
                return profile;
            } else {
                logger.error("Failed to fetch profile for user: {}", username);
                throw new LetterboxdScrapingException("Failed to fetch user profile");
            }
            
        } catch (RestClientException e) {
            logger.error("Network error fetching profile for user: {}", username, e);
            throw new LetterboxdScrapingException("Network error during profile fetch", e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching profile for user: {}", username, e);
            throw new LetterboxdScrapingException("Unexpected error during profile fetch", e);
        }
    }
      /**
     * Validate if a Letterboxd user exists
     */
    @SuppressWarnings("rawtypes")
    public boolean validateUser(String username) {
        if (!properties.isEnabled()) {
            logger.warn("Letterboxd scraper is disabled, assuming user validation fails");
            return false;
        }
        
        try {
            String url = properties.getBaseUrl() + "/api/user/" + username + "/validate";
              ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object exists = response.getBody().get("exists");
                return exists != null && (Boolean) exists;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.warn("Error validating Letterboxd user {}: {}", username, e.getMessage());
            return false;
        }
    }
      /**
     * Check if the scraper service is healthy and responding
     */
    @SuppressWarnings("rawtypes")
    public boolean isScraperHealthy() {
        if (!properties.isEnabled()) {
            return false;
        }
        
        try {
            String healthUrl = properties.getBaseUrl() + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            
            boolean isHealthy = response.getStatusCode().is2xxSuccessful() && 
                               response.getBody() != null && 
                               "healthy".equals(response.getBody().get("status"));
            
            if (isHealthy) {
                logger.debug("Letterboxd scraper service is healthy");
            } else {
                logger.warn("Letterboxd scraper service health check failed");
            }
            
            return isHealthy;
            
        } catch (Exception e) {
            logger.warn("Letterboxd scraper health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get scraper service information
     */
    public Map<String, Object> getScraperInfo() {
        return Map.of(
            "enabled", properties.isEnabled(),
            "baseUrl", properties.getBaseUrl(),
            "timeout", properties.getTimeout(),
            "healthy", isScraperHealthy()
        );
    }
    
    /**
     * Custom exception for Letterboxd scraping errors
     */
    public static class LetterboxdScrapingException extends RuntimeException {
        public LetterboxdScrapingException(String message) {
            super(message);
        }
        
        public LetterboxdScrapingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
