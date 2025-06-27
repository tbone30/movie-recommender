package com.movierecommender.dto.letterboxd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LetterboxdProfile {
    private String username;
    
    @JsonProperty("display_name")
    private String displayName;
    
    private String bio;
    private String location;
    private String website;
    
    @JsonProperty("joined_date")
    private String joinedDate;
    
    @JsonProperty("films_watched")
    private Integer filmsWatched;
    
    private Integer followers;
    private Integer following;
    
    // Constructors
    public LetterboxdProfile() {}
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getJoinedDate() {
        return joinedDate;
    }
    
    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }
    
    public Integer getFilmsWatched() {
        return filmsWatched;
    }
    
    public void setFilmsWatched(Integer filmsWatched) {
        this.filmsWatched = filmsWatched;
    }
    
    public Integer getFollowers() {
        return followers;
    }
    
    public void setFollowers(Integer followers) {
        this.followers = followers;
    }
    
    public Integer getFollowing() {
        return following;
    }
    
    public void setFollowing(Integer following) {
        this.following = following;
    }
}
