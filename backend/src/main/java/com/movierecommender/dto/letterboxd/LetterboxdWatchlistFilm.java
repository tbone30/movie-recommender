package com.movierecommender.dto.letterboxd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LetterboxdWatchlistFilm {
    
    @JsonProperty("film_title")
    private String filmTitle;
    
    @JsonProperty("film_year")
    private Integer filmYear;
    
    @JsonProperty("film_slug")
    private String filmSlug;
    
    private List<String> directors;
    private List<String> genres;
    
    @JsonProperty("added_date")
    private String addedDate;
    
    @JsonProperty("letterboxd_uri")
    private String letterboxdUri;
    
    // Constructors
    public LetterboxdWatchlistFilm() {}
    
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
    
    public List<String> getDirectors() {
        return directors;
    }
    
    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }
    
    public List<String> getGenres() {
        return genres;
    }
    
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
    
    public String getAddedDate() {
        return addedDate;
    }
    
    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }
    
    public String getLetterboxdUri() {
        return letterboxdUri;
    }
    
    public void setLetterboxdUri(String letterboxdUri) {
        this.letterboxdUri = letterboxdUri;
    }
}
