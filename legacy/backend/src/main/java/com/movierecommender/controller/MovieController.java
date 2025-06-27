package com.movierecommender.controller;

import com.movierecommender.entity.Movie;
import com.movierecommender.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Movie> movies = movieService.getTopRatedMovies(pageable);
        
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        try {
            Movie movie = movieService.getMovieById(id);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String query) {
        List<Movie> movies = movieService.searchMovies(query);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/popular")
    public ResponseEntity<Page<Movie>> getPopularMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "5") Long minRatings) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieService.getPopularMovies(minRatings, pageable);
        
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/genres/{genre}")
    public ResponseEntity<Page<Movie>> getMoviesByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieService.getMoviesByGenre(genre, pageable);
        
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        List<String> genres = movieService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/year/{startYear}/{endYear}")
    public ResponseEntity<Page<Movie>> getMoviesByYearRange(
            @PathVariable Integer startYear,
            @PathVariable Integer endYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieService.getMoviesByYearRange(startYear, endYear, pageable);
        
        return ResponseEntity.ok(movies);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        try {
            Movie createdMovie = movieService.createMovie(movie);
            return ResponseEntity.ok(createdMovie);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/metadata")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> updateMovieMetadata(@PathVariable Long id) {
        try {
            Movie updatedMovie = movieService.updateMovieMetadata(id);
            return ResponseEntity.ok(updatedMovie);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}