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
}