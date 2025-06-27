package com.movierecommender.service;

import com.movierecommender.entity.Movie;
import com.movierecommender.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TMDbApiService tmdbApiService;

    public Movie createMovie(Movie movie) {
        // Check if movie already exists by title and year
        Optional<Movie> existingMovie = movieRepository.findByTitleAndYear(movie.getTitle(), movie.getYear());
        if (existingMovie.isPresent()) {
            return existingMovie.get();
        }

        // Enrich with TMDb data if TMDb ID is provided
        if (movie.getTmdbId() != null) {
            Movie enrichedMovie = tmdbApiService.enrichMovieData(movie);
            if (enrichedMovie != null) {
                movie = enrichedMovie;
            }
        }

        return movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findByTmdbId(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId);
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findByLetterboxdId(String letterboxdId) {
        return movieRepository.findByLetterboxdId(letterboxdId);
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findByTitleAndYear(String title, Integer year) {
        return movieRepository.findByTitleAndYear(title, year);
    }

    @Transactional(readOnly = true)
    public List<Movie> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    @Transactional(readOnly = true)
    public Page<Movie> getMoviesByGenre(String genre, Pageable pageable) {
        return movieRepository.findByGenre(genre, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Movie> getMoviesByYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return movieRepository.findByYearBetween(startYear, endYear, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Movie> getTopRatedMovies(Pageable pageable) {
        return movieRepository.findTopRatedMovies(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Movie> getPopularMovies(Long minRatings, Pageable pageable) {
        return movieRepository.findPopularMovies(minRatings, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        return movieRepository.findAllGenres();
    }

    public Movie updateMovieMetadata(Long movieId) {
        Movie movie = getMovieById(movieId);
        
        if (movie.getTmdbId() != null) {
            Movie enrichedMovie = tmdbApiService.enrichMovieData(movie);
            if (enrichedMovie != null) {
                // Update metadata fields
                movie.setOverview(enrichedMovie.getOverview());
                movie.setRuntime(enrichedMovie.getRuntime());
                movie.setPosterUrl(enrichedMovie.getPosterUrl());
                movie.setGenres(enrichedMovie.getGenres());
                movie.setCountries(enrichedMovie.getCountries());
                movie.setLanguages(enrichedMovie.getLanguages());
                movie.setBudget(enrichedMovie.getBudget());
                movie.setRevenue(enrichedMovie.getRevenue());
                movie.setLastMetadataUpdate(LocalDateTime.now());
                
                return movieRepository.save(movie);
            }
        }
        
        return movie;
    }

    public Movie updateMovieRating(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public long getPopularMovieCount(Long minRatings) {
        return movieRepository.countPopularMovies(minRatings);
    }

    public Movie findOrCreateMovie(String title, Integer year, Long tmdbId, String letterboxdId) {
        // Try to find existing movie
        Optional<Movie> existingMovie = Optional.empty();
        
        if (tmdbId != null) {
            existingMovie = findByTmdbId(tmdbId);
        }
        
        if (existingMovie.isEmpty() && letterboxdId != null) {
            existingMovie = findByLetterboxdId(letterboxdId);
        }
        
        if (existingMovie.isEmpty()) {
            existingMovie = findByTitleAndYear(title, year);
        }
        
        if (existingMovie.isPresent()) {
            return existingMovie.get();
        }
        
        // Create new movie
        Movie newMovie = new Movie(title, year);
        newMovie.setTmdbId(tmdbId);
        newMovie.setLetterboxdId(letterboxdId);
        
        return createMovie(newMovie);
    }
}