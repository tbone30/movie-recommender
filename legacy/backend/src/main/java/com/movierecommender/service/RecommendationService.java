package com.movierecommender.service;

import com.movierecommender.entity.Recommendation;
import com.movierecommender.entity.User;
import com.movierecommender.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private UserService userService;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Value("${ml.service.timeout}")
    private int timeout;

    private final WebClient webClient;

    public RecommendationService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public List<Recommendation> generateRecommendationsForUser(Long userId) {
        try {
            String url = String.format("%s/recommend/user/%d", mlServiceUrl, userId);

            logger.info("Generating recommendations for user: {}", userId);

            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null && response.get("recommendations") instanceof List) {
                List<Map<String, Object>> recommendationData = 
                    (List<Map<String, Object>>) response.get("recommendations");
                
                String modelVersion = (String) response.get("model_version");
                String algorithm = (String) response.get("algorithm");

                // Clean up old recommendations for this user
                recommendationRepository.deleteOldRecommendationsForUser(userId, modelVersion);

                // Save new recommendations
                for (int i = 0; i < recommendationData.size(); i++) {
                    Map<String, Object> recData = recommendationData.get(i);
                    
                    Recommendation recommendation = new Recommendation(
                        userId,
                        ((Number) recData.get("movie_id")).longValue(),
                        BigDecimal.valueOf(((Number) recData.get("score")).doubleValue()),
                        algorithm,
                        i + 1, // rank
                        modelVersion
                    );
                    
                    if (recData.get("explanation") != null) {
                        recommendation.setExplanation((String) recData.get("explanation"));
                    }
                    
                    recommendationRepository.save(recommendation);
                }

                return getRecommendationsForUser(userId);
            }

        } catch (WebClientResponseException e) {
            logger.error("ML service error generating recommendations for user {}: {} - {}", 
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to generate recommendations: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error generating recommendations for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to generate recommendations: " + e.getMessage());
        }

        return List.of();
    }

    public List<Recommendation> generateColdStartRecommendations(Long userId, List<String> preferredGenres) {
        try {
            String url = String.format("%s/recommend/cold-start", mlServiceUrl);

            Map<String, Object> requestBody = Map.of(
                "user_id", userId,
                "preferred_genres", preferredGenres != null ? preferredGenres : List.of()
            );

            logger.info("Generating cold start recommendations for user: {}", userId);

            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null && response.get("recommendations") instanceof List) {
                List<Map<String, Object>> recommendationData = 
                    (List<Map<String, Object>>) response.get("recommendations");
                
                String modelVersion = (String) response.get("model_version");
                String algorithm = (String) response.get("algorithm");

                // Save cold start recommendations
                for (int i = 0; i < recommendationData.size(); i++) {
                    Map<String, Object> recData = recommendationData.get(i);
                    
                    Recommendation recommendation = new Recommendation(
                        userId,
                        ((Number) recData.get("movie_id")).longValue(),
                        BigDecimal.valueOf(((Number) recData.get("score")).doubleValue()),
                        algorithm,
                        i + 1, // rank
                        modelVersion
                    );
                    
                    if (recData.get("explanation") != null) {
                        recommendation.setExplanation((String) recData.get("explanation"));
                    }
                    
                    recommendationRepository.save(recommendation);
                }

                return getRecommendationsForUser(userId);
            }

        } catch (WebClientResponseException e) {
            logger.error("ML service error generating cold start recommendations for user {}: {} - {}", 
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to generate cold start recommendations: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error generating cold start recommendations for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to generate cold start recommendations: " + e.getMessage());
        }

        return List.of();
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getRecommendationsForUser(Long userId) {
        return recommendationRepository.findByUserIdOrderByRank(userId);
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getRecommendationsByAlgorithm(Long userId, String algorithm) {
        return recommendationRepository.findByUserIdAndAlgorithm(userId, algorithm);
    }

    @Transactional(readOnly = true)
    public Page<Recommendation> getRecommendationsByScore(Long userId, Pageable pageable) {
        return recommendationRepository.findByUserIdOrderByScore(userId, pageable);
    }

    public void markRecommendationAsViewed(Long recommendationId) {
        recommendationRepository.markAsViewed(recommendationId, LocalDateTime.now());
    }

    public void markRecommendationAsClicked(Long recommendationId) {
        recommendationRepository.markAsClicked(recommendationId, LocalDateTime.now());
    }

    public void hideRecommendation(Long recommendationId) {
        recommendationRepository.hideRecommendation(recommendationId);
    }

    public void cleanupOldRecommendations(String currentModelVersion) {
        recommendationRepository.deleteOldRecommendations(currentModelVersion);
    }

    @Transactional(readOnly = true)
    public long getRecommendationCount(Long userId) {
        return recommendationRepository.countByUserId(userId);
    }

    public void regenerateAllUserRecommendations() {
        logger.info("Starting regeneration of recommendations for all users");
        
        try {
            List<User> activeUsers = userService.getActiveUsersWithLetterboxd();
            logger.info("Found {} active users with Letterboxd profiles", activeUsers.size());
            
            for (User user : activeUsers) {
                try {
                    generateRecommendationsForUser(user.getId());
                    logger.debug("Successfully regenerated recommendations for user: {}", user.getId());
                } catch (Exception e) {
                    logger.error("Failed to regenerate recommendations for user {}: {}", 
                            user.getId(), e.getMessage());
                    // Continue with other users even if one fails
                }
            }
            
            logger.info("Completed regeneration of recommendations for all users");
        } catch (Exception e) {
            logger.error("Error during bulk recommendation regeneration: {}", e.getMessage());
            throw new RuntimeException("Failed to regenerate recommendations for all users: " + e.getMessage());
        }
    }    public BigDecimal getRecommendationAccuracy() {
        // This is a placeholder implementation
        // In a real scenario, you would calculate actual accuracy metrics
        // based on user feedback, ratings, clicks, etc.
        try {
            long totalRecommendations = recommendationRepository.count();
            
            if (totalRecommendations == 0) {
                return BigDecimal.ZERO;
            }
            
            // Simple accuracy metric - placeholder calculation
            // In reality, you'd calculate based on user interactions, feedback, etc.
            BigDecimal accuracy = BigDecimal.valueOf(75.5); // Placeholder value
            
            return accuracy;
        } catch (Exception e) {
            logger.error("Error calculating recommendation accuracy: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}