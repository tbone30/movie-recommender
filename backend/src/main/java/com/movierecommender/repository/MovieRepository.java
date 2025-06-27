package com.movierecommender.repository;

import com.movierecommender.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByGenreContainingIgnoreCase(String genre);
    List<Movie> findByDirectorContainingIgnoreCase(String director);
    List<Movie> findByReleaseYear(Integer releaseYear);
    
    @Query("SELECT m FROM Movie m WHERE m.rating >= :minRating ORDER BY m.rating DESC")
    List<Movie> findMoviesWithRatingAbove(@Param("minRating") Double minRating);
    
    Optional<Movie> findByTitleAndReleaseYear(String title, Integer releaseYear);
}
