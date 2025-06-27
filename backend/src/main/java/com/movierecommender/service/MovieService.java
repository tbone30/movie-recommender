package com.movierecommender.service;

import com.movierecommender.entity.Movie;
import com.movierecommender.repository.MovieRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Long id, Movie movieDetails) {
        return movieRepository.findById(id).map(movie -> {
            movie.setTitle(movieDetails.getTitle());
            movie.setGenre(movieDetails.getGenre());
            movie.setDirector(movieDetails.getDirector());
            movie.setReleaseYear(movieDetails.getReleaseYear());
            movie.setRating(movieDetails.getRating());
            movie.setDescription(movieDetails.getDescription());
            return movieRepository.save(movie);
        }).orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }
}
