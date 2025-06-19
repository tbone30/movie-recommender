package com.movierecommender.repository;

import com.movierecommender.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.isHidden = false ORDER BY r.rank ASC")
    List<Recommendation> findByUserIdOrderByRank(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.algorithm = :algorithm AND r.isHidden = false ORDER BY r.rank ASC")
    List<Recommendation> findByUserIdAndAlgorithm(@Param("userId") Long userId, @Param("algorithm") String algorithm);
    
    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.isHidden = false ORDER BY r.score DESC")
    Page<Recommendation> findByUserIdOrderByScore(@Param("userId") Long userId, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM Recommendation r WHERE r.userId = :userId AND r.modelVersion != :currentVersion")
    void deleteOldRecommendationsForUser(@Param("userId") Long userId, @Param("currentVersion") String currentVersion);
    
    @Modifying
    @Query("DELETE FROM Recommendation r WHERE r.modelVersion != :currentVersion")
    void deleteOldRecommendations(@Param("currentVersion") String currentVersion);
    
    @Modifying
    @Query("UPDATE Recommendation r SET r.viewedAt = :viewedAt WHERE r.id = :id")
    void markAsViewed(@Param("id") Long id, @Param("viewedAt") LocalDateTime viewedAt);
    
    @Modifying
    @Query("UPDATE Recommendation r SET r.clickedAt = :clickedAt WHERE r.id = :id")
    void markAsClicked(@Param("id") Long id, @Param("clickedAt") LocalDateTime clickedAt);
    
    @Modifying
    @Query("UPDATE Recommendation r SET r.isHidden = true WHERE r.id = :id")
    void hideRecommendation(@Param("id") Long id);
    
    @Query("SELECT COUNT(r) FROM Recommendation r WHERE r.userId = :userId AND r.isHidden = false")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Recommendation r WHERE r.movieId = :movieId")
    List<Recommendation> findByMovieId(@Param("movieId") Long movieId);
}