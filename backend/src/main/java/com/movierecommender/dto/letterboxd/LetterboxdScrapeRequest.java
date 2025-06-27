package com.movierecommender.dto.letterboxd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LetterboxdScrapeRequest {
    private String username;
    
    @JsonProperty("include_ratings")
    private boolean includeRatings = true;
    
    @JsonProperty("include_watchlist")
    private boolean includeWatchlist = true;
    
    @JsonProperty("rating_limit")
    private int ratingLimit = 100;
    
    // Constructors
    public LetterboxdScrapeRequest() {}
    
    public LetterboxdScrapeRequest(String username) {
        this.username = username;
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isIncludeRatings() {
        return includeRatings;
    }
    
    public void setIncludeRatings(boolean includeRatings) {
        this.includeRatings = includeRatings;
    }
    
    public boolean isIncludeWatchlist() {
        return includeWatchlist;
    }
    
    public void setIncludeWatchlist(boolean includeWatchlist) {
        this.includeWatchlist = includeWatchlist;
    }
    
    public int getRatingLimit() {
        return ratingLimit;
    }
    
    public void setRatingLimit(int ratingLimit) {
        this.ratingLimit = ratingLimit;
    }
}
