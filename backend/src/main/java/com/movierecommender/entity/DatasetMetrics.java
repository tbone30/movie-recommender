package com.movierecommender.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dataset_metrics", indexes = {
    @Index(name = "idx_last_updated", columnList = "lastUpdated")
})
public class DatasetMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long totalUsers;

    @NotNull
    @Column(nullable = false)
    private Long totalMovies;

    @NotNull
    @Column(nullable = false)
    private Long totalRatings;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(nullable = false, precision = 8, scale = 6)
    private BigDecimal sparsity;

    @Column(nullable = false)
    private Long activeUsers = 0L; // Users with at least 10 ratings

    @Column(nullable = false)
    private Long popularMovies = 0L; // Movies with at least 5 ratings

    @Column(precision = 3, scale = 2)
    private BigDecimal avgRatingsPerUser;

    @Column(precision = 3, scale = 2)
    private BigDecimal avgRatingsPerMovie;

    @Column(precision = 2, scale = 1)
    private BigDecimal avgRatingValue;

    @Column(nullable = false)
    private String modelVersion = "1.0.0";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus trainingStatus = TrainingStatus.PENDING;

    private LocalDateTime lastTrainingStarted;
    private LocalDateTime lastTrainingCompleted;
    private String trainingError;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public enum TrainingStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, SCHEDULED
    }

    // Constructors
    public DatasetMetrics() {}

    public DatasetMetrics(Long totalUsers, Long totalMovies, Long totalRatings, BigDecimal sparsity) {
        this.totalUsers = totalUsers;
        this.totalMovies = totalMovies;
        this.totalRatings = totalRatings;
        this.sparsity = sparsity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalMovies() { return totalMovies; }
    public void setTotalMovies(Long totalMovies) { this.totalMovies = totalMovies; }

    public Long getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

    public BigDecimal getSparsity() { return sparsity; }
    public void setSparsity(BigDecimal sparsity) { this.sparsity = sparsity; }

    public Long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }

    public Long getPopularMovies() { return popularMovies; }
    public void setPopularMovies(Long popularMovies) { this.popularMovies = popularMovies; }

    public BigDecimal getAvgRatingsPerUser() { return avgRatingsPerUser; }
    public void setAvgRatingsPerUser(BigDecimal avgRatingsPerUser) { this.avgRatingsPerUser = avgRatingsPerUser; }

    public BigDecimal getAvgRatingsPerMovie() { return avgRatingsPerMovie; }
    public void setAvgRatingsPerMovie(BigDecimal avgRatingsPerMovie) { this.avgRatingsPerMovie = avgRatingsPerMovie; }

    public BigDecimal getAvgRatingValue() { return avgRatingValue; }
    public void setAvgRatingValue(BigDecimal avgRatingValue) { this.avgRatingValue = avgRatingValue; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public TrainingStatus getTrainingStatus() { return trainingStatus; }
    public void setTrainingStatus(TrainingStatus trainingStatus) { this.trainingStatus = trainingStatus; }

    public LocalDateTime getLastTrainingStarted() { return lastTrainingStarted; }
    public void setLastTrainingStarted(LocalDateTime lastTrainingStarted) { this.lastTrainingStarted = lastTrainingStarted; }

    public LocalDateTime getLastTrainingCompleted() { return lastTrainingCompleted; }
    public void setLastTrainingCompleted(LocalDateTime lastTrainingCompleted) { this.lastTrainingCompleted = lastTrainingCompleted; }

    public String getTrainingError() { return trainingError; }
    public void setTrainingError(String trainingError) { this.trainingError = trainingError; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}