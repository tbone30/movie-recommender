package com.movierecommender.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class LetterboxdSyncService {

    private static final Logger logger = LoggerFactory.getLogger(LetterboxdSyncService.class);

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Value("${ml.service.timeout}")
    private int timeout;

    private final WebClient webClient;

    public LetterboxdSyncService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public Map<String, Object> syncUserData(String letterboxdUsername) {
        try {
            String url = String.format("%s/data/collect/%s", mlServiceUrl, letterboxdUsername);

            logger.info("Starting Letterboxd sync for user: {}", letterboxdUsername);

            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            logger.info("Completed Letterboxd sync for user: {}", letterboxdUsername);
            return response;

        } catch (WebClientResponseException e) {
            logger.error("ML service error syncing user {}: {} - {}", 
                    letterboxdUsername, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to sync Letterboxd data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error syncing Letterboxd data for user {}: {}", letterboxdUsername, e.getMessage());
            throw new RuntimeException("Failed to sync Letterboxd data: " + e.getMessage());
        }
    }

    public Map<String, Object> bulkSyncUsers(List<String> letterboxdUsernames) {
        try {
            String url = String.format("%s/data/bulk-collect", mlServiceUrl);

            Map<String, Object> requestBody = Map.of("usernames", letterboxdUsernames);

            logger.info("Starting bulk Letterboxd sync for {} users", letterboxdUsernames.size());

            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout * 2)) // Double timeout for bulk operations
                    .block();

            logger.info("Completed bulk Letterboxd sync");
            return response;

        } catch (WebClientResponseException e) {
            logger.error("ML service error during bulk sync: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to bulk sync Letterboxd data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during bulk Letterboxd sync: {}", e.getMessage());
            throw new RuntimeException("Failed to bulk sync Letterboxd data: " + e.getMessage());
        }
    }

    public Map<String, Object> getDatasetStats() {
        try {
            String url = String.format("%s/data/stats", mlServiceUrl);

            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (WebClientResponseException e) {
            logger.error("ML service error getting dataset stats: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get dataset stats: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting dataset stats: {}", e.getMessage());
            throw new RuntimeException("Failed to get dataset stats: " + e.getMessage());
        }
    }

    public boolean isServiceHealthy() {
        try {
            String url = String.format("%s/health", mlServiceUrl);
            
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return response != null && "healthy".equals(response.get("status"));

        } catch (Exception e) {
            logger.warn("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
}