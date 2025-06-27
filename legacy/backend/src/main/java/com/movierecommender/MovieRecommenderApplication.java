package com.movierecommender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieRecommenderApplication.class, args);
    }
}