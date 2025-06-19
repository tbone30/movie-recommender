package com.movierecommender.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies", indexes = {
    @Index(name = "idx_tmdb_id", columnList = "tmdbId"),
    @Index(name = "idx_letterboxd_id", columnList = "letterboxdId"),
    @Index(name = "idx_title_year", columnList = "title, year"),
    @Index(name = "idx_avg_rating", columnList = "avgRating")
})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long tmdbId;

    @Column(unique = true)
    private String letterboxdId;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer year;

    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private Set<String> genres = new HashSet<>();

    @Size(max = 255)
    private String director;

    @ElementCollection
    @CollectionTable(name = "movie_actors", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "actor")
    private Set<String> actors = new HashSet<>();

    @Column(length = 500)
    private String posterUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(nullable = false)
    private Long ratingCount = 0L;

    @Column(length = 2000)
    private String overview;

    private Integer runtime; // in minutes

    @ElementCollection
    @CollectionTable(name = "movie_countries", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "country")
    private Set<String> countries = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "movie_languages", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "language")
    private Set<String> languages = new HashSet<>();

    private Long budget;
    private Long revenue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastMetadataUpdate;

    // Constructors
    public Movie() {}

    public Movie(String title, Integer year) {
        this.title = title;
        this.year = year;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public String getLetterboxdId() { return letterboxdId; }
    public void setLetterboxdId(String letterboxdId) { this.letterboxdId = letterboxdId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Set<String> getGenres() { return genres; }
    public void setGenres(Set<String> genres) { this.genres = genres; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public Set<String> getActors() { return actors; }
    public void setActors(Set<String> actors) { this.actors = actors; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }

    public Long getRatingCount() { return ratingCount; }
    public void setRatingCount(Long ratingCount) { this.ratingCount = ratingCount; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }

    public Set<String> getCountries() { return countries; }
    public void setCountries(Set<String> countries) { this.countries = countries; }

    public Set<String> getLanguages() { return languages; }
    public void setLanguages(Set<String> languages) { this.languages = languages; }

    public Long getBudget() { return budget; }
    public void setBudget(Long budget) { this.budget = budget; }

    public Long getRevenue() { return revenue; }
    public void setRevenue(Long revenue) { this.revenue = revenue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastMetadataUpdate() { return lastMetadataUpdate; }
    public void setLastMetadataUpdate(LocalDateTime lastMetadataUpdate) { this.lastMetadataUpdate = lastMetadataUpdate; }
}