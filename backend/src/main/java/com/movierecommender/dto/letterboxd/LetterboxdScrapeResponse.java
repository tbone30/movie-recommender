package com.movierecommender.dto.letterboxd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LetterboxdScrapeResponse {
    private String username;
    private LetterboxdProfile profile;
    private List<LetterboxdRating> ratings;
    private List<LetterboxdWatchlistFilm> watchlist;
    
    @JsonProperty("scraped_at")
    private LocalDateTime scrapedAt;
    
    @JsonProperty("total_ratings")
    private int totalRatings;
    
    @JsonProperty("total_watchlist_items")
    private int totalWatchlistItems;
    
    private boolean success = true;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    // Constructors
    public LetterboxdScrapeResponse() {}
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public LetterboxdProfile getProfile() {
        return profile;
    }
    
    public void setProfile(LetterboxdProfile profile) {
        this.profile = profile;
    }
    
    public List<LetterboxdRating> getRatings() {
        return ratings;
    }
    
    public void setRatings(List<LetterboxdRating> ratings) {
        this.ratings = ratings;
    }
    
    public List<LetterboxdWatchlistFilm> getWatchlist() {
        return watchlist;
    }
    
    public void setWatchlist(List<LetterboxdWatchlistFilm> watchlist) {
        this.watchlist = watchlist;
    }
    
    public LocalDateTime getScrapedAt() {
        return scrapedAt;
    }
    
    public void setScrapedAt(LocalDateTime scrapedAt) {
        this.scrapedAt = scrapedAt;
    }
    
    public int getTotalRatings() {
        return totalRatings;
    }
    
    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
    
    public int getTotalWatchlistItems() {
        return totalWatchlistItems;
    }
    
    public void setTotalWatchlistItems(int totalWatchlistItems) {
        this.totalWatchlistItems = totalWatchlistItems;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
