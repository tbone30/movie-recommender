package com.movierecommender.service;

import com.movierecommender.entity.DatasetMetrics;
import com.movierecommender.repository.DatasetMetricsRepository;
import com.movierecommender.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DatasetService {

    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);

    @Autowired
    private DatasetMetricsRepository datasetMetricsRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MovieService movieService;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Value("${ml.service.timeout}")
    private int timeout;

    @Value("${dataset.min.users}")
    private long minUsersForTraining;

    @Value("${dataset.min.ratings}")
    private long minRatingsForTraining;

    private final WebClient webClient;

    public DatasetService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public DatasetMetrics calculateAndSaveDatasetMetrics() {
        logger.info("Calculating dataset metrics");

        long totalUsers = userService.getActiveUserCount();
        long totalMovies = movieService.getPopularMovieCount(1L); // Movies with at least 1 rating
        long totalRatings = ratingRepository.count();
        long activeUsers = ratingRepository.countDistinctUsers();
        long popularMovies = movieService.getPopularMovieCount(5L); // Movies with at least 5 ratings

        // Calculate sparsity: (total_ratings / (total_users * total_movies))
        BigDecimal sparsity = BigDecimal.ZERO;
        if (totalUsers > 0 && totalMovies > 0) {
            BigDecimal totalPossibleRatings = BigDecimal.valueOf(totalUsers).multiply(BigDecimal.valueOf(totalMovies));
            sparsity = BigDecimal.valueOf(totalRatings)
                    .divide(totalPossibleRatings, 6, RoundingMode.HALF_UP);
        }

        // Calculate averages
        BigDecimal avgRatingsPerUser = totalUsers > 0 ? 
            BigDecimal.valueOf(totalRatings).divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;

        BigDecimal avgRatingsPerMovie = totalMovies > 0 ? 
            BigDecimal.valueOf(totalRatings).divide(BigDecimal.valueOf(totalMovies), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;

        BigDecimal avgRatingValue = ratingRepository.findOverallAverageRating();
        if (avgRatingValue == null) {
            avgRatingValue = BigDecimal.ZERO;
        }

        DatasetMetrics metrics = new DatasetMetrics(totalUsers, totalMovies, totalRatings, sparsity);
        metrics.setActiveUsers(activeUsers);
        metrics.setPopularMovies(popularMovies);
        metrics.setAvgRatingsPerUser(avgRatingsPerUser);
        metrics.setAvgRatingsPerMovie(avgRatingsPerMovie);
        metrics.setAvgRatingValue(avgRatingValue);

        DatasetMetrics saved = datasetMetricsRepository.save(metrics);
        logger.info("Dataset metrics calculated and saved: {} users, {} movies, {} ratings, sparsity: {}", 
                totalUsers, totalMovies, totalRatings, sparsity);

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<DatasetMetrics> getLatestMetrics() {
        return datasetMetricsRepository.findLatest();
    }

    @Transactional
    public DatasetMetrics getDatasetMetrics() {
        Optional<DatasetMetrics> latestMetrics = getLatestMetrics();
        if (latestMetrics.isPresent()) {
            return latestMetrics.get();
        } else {
            // If no metrics exist, calculate and save new ones
            return calculateAndSaveDatasetMetrics();
        }
    }

    @Transactional(readOnly = true)
    public boolean isReadyForTraining() {
        Optional<DatasetMetrics> metrics = getLatestMetrics();
        if (metrics.isEmpty()) {
            return false;
        }

        DatasetMetrics m = metrics.get();
        return m.getTotalUsers() >= minUsersForTraining && 
               m.getTotalRatings() >= minRatingsForTraining;
    }

    public Map<String, Object> triggerModelTraining() {
        try {
            if (!isReadyForTraining()) {
                throw new RuntimeException("Dataset not ready for training. Need at least " + 
                        minUsersForTraining + " users and " + minRatingsForTraining + " ratings");
            }

            // Update training status
            DatasetMetrics metrics = getLatestMetrics().orElseThrow();
            metrics.setTrainingStatus(DatasetMetrics.TrainingStatus.IN_PROGRESS);
            metrics.setLastTrainingStarted(LocalDateTime.now());
            metrics.setTrainingError(null);
            datasetMetricsRepository.save(metrics);

            String url = String.format("%s/model/train", mlServiceUrl);

            logger.info("Triggering model training");

            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout * 5)) // Extended timeout for training
                    .block();

            // Update training completion status
            metrics.setTrainingStatus(DatasetMetrics.TrainingStatus.COMPLETED);
            metrics.setLastTrainingCompleted(LocalDateTime.now());
            if (response != null && response.get("model_version") != null) {
                metrics.setModelVersion((String) response.get("model_version"));
            }
            datasetMetricsRepository.save(metrics);

            logger.info("Model training completed successfully");
            return response;

        } catch (WebClientResponseException e) {
            logger.error("ML service error during training: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            
            // Update training error status
            getLatestMetrics().ifPresent(metrics -> {
                metrics.setTrainingStatus(DatasetMetrics.TrainingStatus.FAILED);
                metrics.setTrainingError(e.getResponseBodyAsString());
                datasetMetricsRepository.save(metrics);
            });
            
            throw new RuntimeException("Failed to train model: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during model training: {}", e.getMessage());
            
            // Update training error status
            getLatestMetrics().ifPresent(metrics -> {
                metrics.setTrainingStatus(DatasetMetrics.TrainingStatus.FAILED);
                metrics.setTrainingError(e.getMessage());
                datasetMetricsRepository.save(metrics);
            });
            
            throw new RuntimeException("Failed to train model: " + e.getMessage());
        }
    }

    public Map<String, Object> getTrainingStatus() {
        try {
            String url = String.format("%s/model/status", mlServiceUrl);

            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (WebClientResponseException e) {
            logger.error("ML service error getting training status: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get training status: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting training status: {}", e.getMessage());
            throw new RuntimeException("Failed to get training status: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public boolean shouldRetrain() {
        Optional<DatasetMetrics> currentMetrics = getLatestMetrics();
        if (currentMetrics.isEmpty()) {
            return false;
        }

        DatasetMetrics metrics = currentMetrics.get();
        
        // Don't retrain if already in progress
        if (metrics.getTrainingStatus() == DatasetMetrics.TrainingStatus.IN_PROGRESS) {
            return false;
        }

        // Retrain if never trained before
        if (metrics.getLastTrainingCompleted() == null) {
            return isReadyForTraining();
        }

        // Retrain if data has grown significantly (configurable threshold)
        // This is a simplified check - in production you might want more sophisticated logic
        LocalDateTime lastTraining = metrics.getLastTrainingCompleted();
        return lastTraining.isBefore(LocalDateTime.now().minusDays(7)) && isReadyForTraining();
    }

    public void rebuildDataset() {
        logger.info("Initiating dataset rebuild");
        
        try {
            // Calculate and save new dataset metrics
            DatasetMetrics newMetrics = calculateAndSaveDatasetMetrics();
            logger.info("Dataset metrics recalculated: {} users, {} movies, {} ratings", 
                    newMetrics.getTotalUsers(), newMetrics.getTotalMovies(), newMetrics.getTotalRatings());
            
            // If the dataset is ready for training, trigger a rebuild in the ML service
            if (isReadyForTraining()) {
                String url = String.format("%s/dataset/rebuild", mlServiceUrl);
                
                Map<String, Object> response = webClient.post()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofMillis(timeout * 2)) // Extended timeout for rebuild
                        .block();
                
                logger.info("Dataset rebuild completed successfully: {}", response);
            } else {
                logger.warn("Dataset not ready for training. Rebuild skipped.");
            }
            
        } catch (WebClientResponseException e) {
            logger.error("ML service error during dataset rebuild: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to rebuild dataset: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during dataset rebuild: {}", e.getMessage());
            throw new RuntimeException("Failed to rebuild dataset: " + e.getMessage());
        }
    }

    public Map<String, Object> getMLServiceStatus() {
        try {
            String url = String.format("%s/status", mlServiceUrl);
            
            logger.info("Getting ML service status");
            
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            
            if (response != null) {
                logger.info("ML service status retrieved successfully");
                return response;
            } else {
                return Map.of("status", "unavailable", "message", "No response from ML service");
            }
            
        } catch (WebClientResponseException e) {
            logger.error("ML service error getting status: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of(
                "status", "error",
                "message", "ML service returned error: " + e.getStatusCode(),
                "error", e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            logger.error("Error getting ML service status: {}", e.getMessage());
            return Map.of(
                "status", "unavailable",
                "message", "Failed to connect to ML service",
                "error", e.getMessage()
            );
        }
    }

    public Map<String, Object> getDatasetHealth() {
        try {
            logger.info("Calculating dataset health metrics");
            
            // Get current dataset metrics
            Optional<DatasetMetrics> metricsOpt = getLatestMetrics();
            if (metricsOpt.isEmpty()) {
                return Map.of(
                    "status", "unhealthy",
                    "message", "No dataset metrics available",
                    "score", 0
                );
            }
            
            DatasetMetrics metrics = metricsOpt.get();
            
            // Calculate health score based on various factors
            int healthScore = 100;
            String status = "healthy";
            String message = "Dataset is in good health";
            
            // Check if dataset meets minimum requirements
            if (metrics.getTotalUsers() < minUsersForTraining) {
                healthScore -= 30;
                status = "warning";
                message = "Insufficient users for training";
            }
            
            if (metrics.getTotalRatings() < minRatingsForTraining) {
                healthScore -= 30;
                status = "warning";
                message = "Insufficient ratings for training";
            }
            
            // Check sparsity - if too sparse, reduce health score
            if (metrics.getSparsity().compareTo(BigDecimal.valueOf(0.001)) < 0) {
                healthScore -= 20;
                if (status.equals("healthy")) {
                    status = "warning";
                    message = "Dataset is very sparse";
                }
            }
            
            // Check training status
            if (metrics.getTrainingStatus() == DatasetMetrics.TrainingStatus.FAILED) {
                healthScore -= 40;
                status = "unhealthy";
                message = "Last training failed";
            }
            
            // Determine final status based on score
            if (healthScore < 50) {
                status = "unhealthy";
            } else if (healthScore < 80) {
                status = "warning";
            }
            
            return Map.of(
                "status", status,
                "score", Math.max(0, healthScore),
                "message", message,
                "details", Map.of(
                    "total_users", metrics.getTotalUsers(),
                    "total_ratings", metrics.getTotalRatings(),
                    "total_movies", metrics.getTotalMovies(),
                    "sparsity", metrics.getSparsity(),
                    "training_status", metrics.getTrainingStatus() != null ? 
                        metrics.getTrainingStatus().toString() : "UNKNOWN",
                    "last_updated", metrics.getCreatedAt()
                )
            );
            
        } catch (Exception e) {
            logger.error("Error calculating dataset health: {}", e.getMessage());
            return Map.of(
                "status", "error",
                "message", "Failed to calculate dataset health",
                "score", 0,
                "error", e.getMessage()
            );
        }
    }    public void performSystemCleanup() {
        logger.info("Starting system cleanup");
        
        try {
            // Recalculate dataset metrics to ensure they're up to date
            DatasetMetrics newMetrics = calculateAndSaveDatasetMetrics();
            logger.info("Dataset metrics recalculated and saved");
            
            // Clean up old recommendations if we have a current model version
            if (newMetrics.getModelVersion() != null) {
                // This would typically call RecommendationService to clean old recommendations
                logger.info("Current model version: {}", newMetrics.getModelVersion());
            }
            
            // Trigger ML service cleanup if available
            try {
                String url = String.format("%s/cleanup", mlServiceUrl);
                
                Map<String, Object> response = webClient.post()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();
                
                if (response != null) {
                    logger.info("ML service cleanup completed: {}", response);
                }
            } catch (Exception e) {
                logger.warn("ML service cleanup failed (non-critical): {}", e.getMessage());
            }
            
            logger.info("System cleanup completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during system cleanup: {}", e.getMessage());
            throw new RuntimeException("System cleanup failed: " + e.getMessage());
        }
    }

    public void triggerBulkDataCollection(List<String> usernames) {
        logger.info("Starting bulk data collection for {} usernames", usernames.size());
        
        try {
            // Call ML service to trigger bulk data collection
            String url = String.format("%s/data/collect/bulk", mlServiceUrl);
            
            Map<String, Object> requestBody = Map.of(
                "usernames", usernames,
                "timestamp", LocalDateTime.now().toString()
            );
            
            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout * 3)) // Extended timeout for bulk operations
                    .block();
            
            if (response != null) {
                logger.info("Bulk data collection initiated successfully: {}", response);
                
                // If the response contains information about successful collections, log it
                if (response.get("processed_count") != null) {
                    logger.info("Successfully processed {} usernames for data collection", 
                            response.get("processed_count"));
                }
                
                if (response.get("failed_count") != null && 
                    ((Number) response.get("failed_count")).intValue() > 0) {
                    logger.warn("Failed to process {} usernames for data collection", 
                            response.get("failed_count"));
                }
            } else {
                logger.warn("No response received from ML service for bulk data collection");
            }
            
        } catch (WebClientResponseException e) {
            logger.error("ML service error during bulk data collection: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to trigger bulk data collection: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during bulk data collection: {}", e.getMessage());
            throw new RuntimeException("Failed to trigger bulk data collection: " + e.getMessage());
        }
    }
}