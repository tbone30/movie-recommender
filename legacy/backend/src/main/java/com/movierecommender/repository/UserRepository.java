package com.movierecommender.repository;

import com.movierecommender.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByLetterboxdUsername(String letterboxdUsername);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByLetterboxdUsername(String letterboxdUsername);
    
    @Query("SELECT u FROM User u WHERE u.syncStatus = :status AND u.isActive = true")
    List<User> findBySyncStatus(@Param("status") User.SyncStatus status);
    
    @Query("SELECT u FROM User u WHERE u.lastSyncDate IS NULL OR u.lastSyncDate < :cutoffDate")
    List<User> findUsersNeedingSync(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT u FROM User u WHERE u.letterboxdUsername IS NOT NULL AND u.isActive = true")
    List<User> findActiveUsersWithLetterboxd();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
}