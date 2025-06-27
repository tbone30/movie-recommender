package com.movierecommender.dto.letterboxd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LetterboxdRating {
    
    @JsonProperty("film_title")
    private String filmTitle;
    
    @JsonProperty("film_year")
    private Integer filmYear;
    
    @JsonProperty("film_slug")
    private String filmSlug;
    
    private Double rating;
    
    @JsonProperty("watched_date")
    private String watchedDate;
    
    private String review;
    
    @JsonProperty("letterboxd_uri")
    private String letterboxdUri;
    
    // Constructors
    public LetterboxdRating() {}
    
    // Getters and setters
    public String getFilmTitle() {
        return filmTitle;
    }
    
    public void setFilmTitle(String filmTitle) {
        this.filmTitle = filmTitle;
    }
    
    public Integer getFilmYear() {
        return filmYear;
    }
    
    public void setFilmYear(Integer filmYear) {
        this.filmYear = filmYear;
    }
    
    public String getFilmSlug() {
        return filmSlug;
    }
    
    public void setFilmSlug(String filmSlug) {
        this.filmSlug = filmSlug;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getWatchedDate() {
        return watchedDate;
    }
    
    public void setWatchedDate(String watchedDate) {
        this.watchedDate = watchedDate;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
    
    public String getLetterboxdUri() {
        return letterboxdUri;
    }
    
    public void setLetterboxdUri(String letterboxdUri) {
        this.letterboxdUri = letterboxdUri;
    }
}
