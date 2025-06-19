package com.movierecommender.repository;

import com.movierecommender.entity.DatasetMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetMetricsRepository extends JpaRepository<DatasetMetrics, Long> {
    
    @Query("SELECT d FROM DatasetMetrics d ORDER BY d.lastUpdated DESC")
    Optional<DatasetMetrics> findLatest();
    
    @Query("SELECT d FROM DatasetMetrics d WHERE d.trainingStatus = :status ORDER BY d.lastUpdated DESC")
    Optional<DatasetMetrics> findLatestByTrainingStatus(DatasetMetrics.TrainingStatus status);
    
    boolean existsByTrainingStatus(DatasetMetrics.TrainingStatus status);
}