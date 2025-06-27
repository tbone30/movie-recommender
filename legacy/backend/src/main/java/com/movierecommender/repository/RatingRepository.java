package com.movierecommender.repository;

import com.movierecommender.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    List<Rating> findByUserId(Long userId);
    
    Page<Rating> findByUserId(Long userId, Pageable pageable);
    
    List<Rating> findByMovieId(Long movieId);
    
    Optional<Rating> findByUserIdAndMovieIdAndWatchedDate(Long userId, Long movieId, LocalDate watchedDate);
    
    @Query("SELECT r FROM Rating r WHERE r.userId = :userId AND r.movieId = :movieId ORDER BY r.watchedDate DESC")
    List<Rating> findByUserIdAndMovieIdOrderByWatchedDateDesc(@Param("userId") Long userId, @Param("movieId") Long movieId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.movieId = :movieId")
    long countByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.movieId = :movieId")
    BigDecimal findAverageRatingByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT r FROM Rating r WHERE r.userId = :userId ORDER BY r.rating DESC")
    List<Rating> findTopRatedByUser(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT r FROM Rating r WHERE r.reviewText IS NOT NULL AND r.isPublic = true ORDER BY r.createdAt DESC")
    Page<Rating> findRecentReviews(Pageable pageable);
    
    @Query("SELECT r FROM Rating r WHERE r.userId = :userId AND r.watchedDate BETWEEN :startDate AND :endDate")
    List<Rating> findByUserIdAndWatchedDateBetween(@Param("userId") Long userId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT r.userId) FROM Rating r")
    long countDistinctUsers();
    
    @Query("SELECT COUNT(DISTINCT r.movieId) FROM Rating r")
    long countDistinctMovies();
    
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Rating r")
    BigDecimal findOverallAverageRating();
}