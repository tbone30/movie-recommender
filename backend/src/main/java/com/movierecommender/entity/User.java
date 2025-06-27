package com.movierecommender.entity;

public class User {
    private int id;
    private String username;
    private String email;
    private String letterBoxdUsername;

    public User(int id, String username, String email, String letterBoxdUsername) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.letterBoxdUsername = letterBoxdUsername;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLetterBoxdUsername() {
        return letterBoxdUsername;
    }

    public void setLetterBoxdUsername(String letterBoxdUsername) {
        this.letterBoxdUsername = letterBoxdUsername;
    }
}
