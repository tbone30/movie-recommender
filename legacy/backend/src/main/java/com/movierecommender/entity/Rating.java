package com.movierecommender.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings", indexes = {
    @Index(name = "idx_user_movie", columnList = "userId, movieId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_movie_id", columnList = "movieId"),
    @Index(name = "idx_watched_date", columnList = "watchedDate"),
    @Index(name = "idx_rating_value", columnList = "rating")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "movieId", "watchedDate"})
})
public class Rating {

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
    @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(length = 2000)
    private String reviewText;

    @NotNull
    @Column(nullable = false)
    private LocalDate watchedDate;

    @Column(nullable = false)
    private Boolean isRewatch = false;

    @Column(nullable = false)
    private Boolean isPublic = true;

    private Integer likes = 0;

    private String letterboxdReviewId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Constructors
    public Rating() {}

    public Rating(Long userId, Long movieId, BigDecimal rating, LocalDate watchedDate) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
        this.watchedDate = watchedDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public LocalDate getWatchedDate() { return watchedDate; }
    public void setWatchedDate(LocalDate watchedDate) { this.watchedDate = watchedDate; }

    public Boolean getIsRewatch() { return isRewatch; }
    public void setIsRewatch(Boolean isRewatch) { this.isRewatch = isRewatch; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public String getLetterboxdReviewId() { return letterboxdReviewId; }
    public void setLetterboxdReviewId(String letterboxdReviewId) { this.letterboxdReviewId = letterboxdReviewId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}