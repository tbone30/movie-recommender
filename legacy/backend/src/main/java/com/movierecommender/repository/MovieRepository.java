package com.movierecommender.repository;

import com.movierecommender.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    Optional<Movie> findByTmdbId(Long tmdbId);
    
    Optional<Movie> findByLetterboxdId(String letterboxdId);
    
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Movie> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    @Query("SELECT m FROM Movie m WHERE m.title = :title AND m.year = :year")
    Optional<Movie> findByTitleAndYear(@Param("title") String title, @Param("year") Integer year);
    
    @Query("SELECT m FROM Movie m WHERE :genre MEMBER OF m.genres")
    Page<Movie> findByGenre(@Param("genre") String genre, Pageable pageable);
    
    @Query("SELECT m FROM Movie m WHERE m.year BETWEEN :startYear AND :endYear")
    Page<Movie> findByYearBetween(@Param("startYear") Integer startYear, 
                                  @Param("endYear") Integer endYear, 
                                  Pageable pageable);
    
    @Query("SELECT m FROM Movie m ORDER BY m.avgRating DESC, m.ratingCount DESC")
    Page<Movie> findTopRatedMovies(Pageable pageable);
    
    @Query("SELECT m FROM Movie m WHERE m.ratingCount >= :minRatings ORDER BY m.ratingCount DESC")
    Page<Movie> findPopularMovies(@Param("minRatings") Long minRatings, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.ratingCount >= :minRatings")
    long countPopularMovies(@Param("minRatings") Long minRatings);
    
    @Query("SELECT DISTINCT g FROM Movie m JOIN m.genres g ORDER BY g")
    List<String> findAllGenres();
}