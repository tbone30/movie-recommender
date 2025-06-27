package com.movierecommender.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "letterboxd_username")
    private String letterboxdUsername;

    // Default constructor
    public User() {}

    public User(String username, String email, String letterboxdUsername) {
        this.username = username;
        this.email = email;
        this.letterboxdUsername = letterboxdUsername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getLetterboxdUsername() {
        return letterboxdUsername;
    }

    public void setLetterboxdUsername(String letterboxdUsername) {
        this.letterboxdUsername = letterboxdUsername;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", letterboxdUsername='" + letterboxdUsername + '\'' +
                '}';
    }
}
