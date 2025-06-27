package com.movierecommender.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations", indexes = {
    @Index(name = "idx_user_score", columnList = "userId, score"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_movie_id", columnList = "movieId"),
    @Index(name = "idx_algorithm", columnList = "algorithm"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @NotNull
    @Column(nullable = false)
    private Long movieId;

    @NotNull
    @DecimalMin(value = "0.0", message = "Score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Score must not exceed 1.0")
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @NotNull
    @Column(nullable = false, length = 50)
    private String algorithm;

    @Column(length = 500)
    private String explanation;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private String modelVersion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime viewedAt;
    private LocalDateTime clickedAt;
    private Boolean isHidden = false;

    // Constructors
    public Recommendation() {}

    public Recommendation(Long userId, Long movieId, BigDecimal score, String algorithm, Integer rank, String modelVersion) {
        this.userId = userId;
        this.movieId = movieId;
        this.score = score;
        this.algorithm = algorithm;
        this.rank = rank;
        this.modelVersion = modelVersion;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }

    public LocalDateTime getClickedAt() { return clickedAt; }
    public void setClickedAt(LocalDateTime clickedAt) { this.clickedAt = clickedAt; }

    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }
}