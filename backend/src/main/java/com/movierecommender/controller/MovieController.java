package com.movierecommender.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movierecommender.entity.Movie;
import com.movierecommender.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @RequestMapping("/getAll")
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }
    
    public Movie getMovieById(Long id) {
        return movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    public Movie createMovie(Movie movie) {
        return movieService.createMovie(movie);
    }

    public Movie updateMovie(Long id, Movie movieDetails) {
        return movieService.updateMovie(id, movieDetails);
    }

    public void deleteMovie(Long id) {
        movieService.deleteMovie(id);
    }
}
