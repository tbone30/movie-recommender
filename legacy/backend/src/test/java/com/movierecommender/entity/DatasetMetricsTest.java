package com.movierecommender.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DatasetMetrics Entity Tests")
class DatasetMetricsTest {

    private static Validator validator;
    private DatasetMetrics datasetMetrics;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        datasetMetrics = new DatasetMetrics();
        datasetMetrics.setTotalUsers(1000L);
        datasetMetrics.setTotalMovies(500L);
        datasetMetrics.setTotalRatings(25000L);
        datasetMetrics.setSparsity(new BigDecimal("0.950000"));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create dataset metrics with default values")
        void defaultConstructor_ShouldCreateDatasetMetricsWithDefaultValues() {
            DatasetMetrics datasetMetrics = new DatasetMetrics();

            assertThat(datasetMetrics.getId()).isNull();
            assertThat(datasetMetrics.getTotalUsers()).isNull();
            assertThat(datasetMetrics.getTotalMovies()).isNull();
            assertThat(datasetMetrics.getTotalRatings()).isNull();
            assertThat(datasetMetrics.getSparsity()).isNull();
            assertThat(datasetMetrics.getActiveUsers()).isEqualTo(0L);
            assertThat(datasetMetrics.getPopularMovies()).isEqualTo(0L);
            assertThat(datasetMetrics.getAvgRatingsPerUser()).isNull();
            assertThat(datasetMetrics.getAvgRatingsPerMovie()).isNull();
            assertThat(datasetMetrics.getAvgRatingValue()).isNull();
            assertThat(datasetMetrics.getModelVersion()).isEqualTo("1.0.0");
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.PENDING);
            assertThat(datasetMetrics.getLastTrainingStarted()).isNull();
            assertThat(datasetMetrics.getLastTrainingCompleted()).isNull();
            assertThat(datasetMetrics.getTrainingError()).isNull();
            assertThat(datasetMetrics.getCreatedAt()).isNull();
            assertThat(datasetMetrics.getLastUpdated()).isNull();
        }

        @Test
        @DisplayName("Parameterized constructor should set required fields")
        void parameterizedConstructor_ShouldSetRequiredFields() {
            Long totalUsers = 2000L;
            Long totalMovies = 1000L;
            Long totalRatings = 50000L;
            BigDecimal sparsity = new BigDecimal("0.975000");

            DatasetMetrics datasetMetrics = new DatasetMetrics(totalUsers, totalMovies, totalRatings, sparsity);

            assertThat(datasetMetrics.getTotalUsers()).isEqualTo(totalUsers);
            assertThat(datasetMetrics.getTotalMovies()).isEqualTo(totalMovies);
            assertThat(datasetMetrics.getTotalRatings()).isEqualTo(totalRatings);
            assertThat(datasetMetrics.getSparsity()).isEqualByComparingTo(sparsity);
            assertThat(datasetMetrics.getActiveUsers()).isEqualTo(0L);
            assertThat(datasetMetrics.getPopularMovies()).isEqualTo(0L);
            assertThat(datasetMetrics.getModelVersion()).isEqualTo("1.0.0");
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid dataset metrics should pass validation")
        void validDatasetMetrics_ShouldPassValidation() {
            Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
            assertThat(violations).isEmpty();
        }

        @Nested
        @DisplayName("Total Users Validation")
        class TotalUsersValidationTests {

            @Test
            @DisplayName("Should reject null total users")
            void nullTotalUsers_ShouldFailValidation() {
                datasetMetrics.setTotalUsers(null);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid total users")
            void validTotalUsers_ShouldPassValidation() {
                datasetMetrics.setTotalUsers(5000L);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Total Movies Validation")
        class TotalMoviesValidationTests {

            @Test
            @DisplayName("Should reject null total movies")
            void nullTotalMovies_ShouldFailValidation() {
                datasetMetrics.setTotalMovies(null);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid total movies")
            void validTotalMovies_ShouldPassValidation() {
                datasetMetrics.setTotalMovies(2000L);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Total Ratings Validation")
        class TotalRatingsValidationTests {

            @Test
            @DisplayName("Should reject null total ratings")
            void nullTotalRatings_ShouldFailValidation() {
                datasetMetrics.setTotalRatings(null);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid total ratings")
            void validTotalRatings_ShouldPassValidation() {
                datasetMetrics.setTotalRatings(100000L);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Sparsity Validation")
        class SparsityValidationTests {

            @Test
            @DisplayName("Should reject null sparsity")
            void nullSparsity_ShouldFailValidation() {
                datasetMetrics.setSparsity(null);
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should reject sparsity below minimum")
            void sparsityBelowMinimum_ShouldFailValidation() {
                datasetMetrics.setSparsity(new BigDecimal("-0.1"));
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 0.0");
            }

            @Test
            @DisplayName("Should reject sparsity above maximum")
            void sparsityAboveMaximum_ShouldFailValidation() {
                datasetMetrics.setSparsity(new BigDecimal("1.1"));
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must be less than or equal to 1.0");
            }

            @Test
            @DisplayName("Should accept minimum sparsity")
            void minimumSparsity_ShouldPassValidation() {
                datasetMetrics.setSparsity(new BigDecimal("0.0"));
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept maximum sparsity")
            void maximumSparsity_ShouldPassValidation() {
                datasetMetrics.setSparsity(new BigDecimal("1.0"));
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid sparsity in range")
            void validSparsityInRange_ShouldPassValidation() {
                datasetMetrics.setSparsity(new BigDecimal("0.123456"));
                Set<ConstraintViolation<DatasetMetrics>> violations = validator.validate(datasetMetrics);
                assertThat(violations).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Should get and set id")
        void shouldGetAndSetId() {
            Long id = 123L;
            datasetMetrics.setId(id);
            assertThat(datasetMetrics.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should get and set total users")
        void shouldGetAndSetTotalUsers() {
            Long totalUsers = 5000L;
            datasetMetrics.setTotalUsers(totalUsers);
            assertThat(datasetMetrics.getTotalUsers()).isEqualTo(totalUsers);
        }

        @Test
        @DisplayName("Should get and set total movies")
        void shouldGetAndSetTotalMovies() {
            Long totalMovies = 2500L;
            datasetMetrics.setTotalMovies(totalMovies);
            assertThat(datasetMetrics.getTotalMovies()).isEqualTo(totalMovies);
        }

        @Test
        @DisplayName("Should get and set total ratings")
        void shouldGetAndSetTotalRatings() {
            Long totalRatings = 125000L;
            datasetMetrics.setTotalRatings(totalRatings);
            assertThat(datasetMetrics.getTotalRatings()).isEqualTo(totalRatings);
        }

        @Test
        @DisplayName("Should get and set sparsity")
        void shouldGetAndSetSparsity() {
            BigDecimal sparsity = new BigDecimal("0.987654");
            datasetMetrics.setSparsity(sparsity);
            assertThat(datasetMetrics.getSparsity()).isEqualByComparingTo(sparsity);
        }

        @Test
        @DisplayName("Should get and set active users")
        void shouldGetAndSetActiveUsers() {
            Long activeUsers = 750L;
            datasetMetrics.setActiveUsers(activeUsers);
            assertThat(datasetMetrics.getActiveUsers()).isEqualTo(activeUsers);
        }

        @Test
        @DisplayName("Should get and set popular movies")
        void shouldGetAndSetPopularMovies() {
            Long popularMovies = 300L;
            datasetMetrics.setPopularMovies(popularMovies);
            assertThat(datasetMetrics.getPopularMovies()).isEqualTo(popularMovies);
        }

        @Test
        @DisplayName("Should get and set avg ratings per user")
        void shouldGetAndSetAvgRatingsPerUser() {
            BigDecimal avgRatingsPerUser = new BigDecimal("25.50");
            datasetMetrics.setAvgRatingsPerUser(avgRatingsPerUser);
            assertThat(datasetMetrics.getAvgRatingsPerUser()).isEqualByComparingTo(avgRatingsPerUser);
        }

        @Test
        @DisplayName("Should get and set avg ratings per movie")
        void shouldGetAndSetAvgRatingsPerMovie() {
            BigDecimal avgRatingsPerMovie = new BigDecimal("50.25");
            datasetMetrics.setAvgRatingsPerMovie(avgRatingsPerMovie);
            assertThat(datasetMetrics.getAvgRatingsPerMovie()).isEqualByComparingTo(avgRatingsPerMovie);
        }

        @Test
        @DisplayName("Should get and set avg rating value")
        void shouldGetAndSetAvgRatingValue() {
            BigDecimal avgRatingValue = new BigDecimal("3.7");
            datasetMetrics.setAvgRatingValue(avgRatingValue);
            assertThat(datasetMetrics.getAvgRatingValue()).isEqualByComparingTo(avgRatingValue);
        }

        @Test
        @DisplayName("Should get and set model version")
        void shouldGetAndSetModelVersion() {
            String modelVersion = "2.3.1";
            datasetMetrics.setModelVersion(modelVersion);
            assertThat(datasetMetrics.getModelVersion()).isEqualTo(modelVersion);
        }

        @Test
        @DisplayName("Should get and set training status")
        void shouldGetAndSetTrainingStatus() {
            DatasetMetrics.TrainingStatus trainingStatus = DatasetMetrics.TrainingStatus.COMPLETED;
            datasetMetrics.setTrainingStatus(trainingStatus);
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(trainingStatus);
        }

        @Test
        @DisplayName("Should get and set last training started")
        void shouldGetAndSetLastTrainingStarted() {
            LocalDateTime lastTrainingStarted = LocalDateTime.now();
            datasetMetrics.setLastTrainingStarted(lastTrainingStarted);
            assertThat(datasetMetrics.getLastTrainingStarted()).isEqualTo(lastTrainingStarted);
        }

        @Test
        @DisplayName("Should get and set last training completed")
        void shouldGetAndSetLastTrainingCompleted() {
            LocalDateTime lastTrainingCompleted = LocalDateTime.now();
            datasetMetrics.setLastTrainingCompleted(lastTrainingCompleted);
            assertThat(datasetMetrics.getLastTrainingCompleted()).isEqualTo(lastTrainingCompleted);
        }

        @Test
        @DisplayName("Should get and set training error")
        void shouldGetAndSetTrainingError() {
            String trainingError = "Out of memory during matrix factorization";
            datasetMetrics.setTrainingError(trainingError);
            assertThat(datasetMetrics.getTrainingError()).isEqualTo(trainingError);
        }

        @Test
        @DisplayName("Should get and set created at")
        void shouldGetAndSetCreatedAt() {
            LocalDateTime createdAt = LocalDateTime.now();
            datasetMetrics.setCreatedAt(createdAt);
            assertThat(datasetMetrics.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should get and set last updated")
        void shouldGetAndSetLastUpdated() {
            LocalDateTime lastUpdated = LocalDateTime.now();
            datasetMetrics.setLastUpdated(lastUpdated);
            assertThat(datasetMetrics.getLastUpdated()).isEqualTo(lastUpdated);
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("TrainingStatus enum should have all expected values")
        void trainingStatusEnum_ShouldHaveAllExpectedValues() {
            DatasetMetrics.TrainingStatus[] expectedValues = {
                DatasetMetrics.TrainingStatus.PENDING,
                DatasetMetrics.TrainingStatus.IN_PROGRESS,
                DatasetMetrics.TrainingStatus.COMPLETED,
                DatasetMetrics.TrainingStatus.FAILED,
                DatasetMetrics.TrainingStatus.SCHEDULED
            };
            
            assertThat(DatasetMetrics.TrainingStatus.values()).containsExactly(expectedValues);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Active users should default to 0")
        void newDatasetMetrics_ShouldHaveZeroActiveUsers() {
            DatasetMetrics newDatasetMetrics = new DatasetMetrics();
            assertThat(newDatasetMetrics.getActiveUsers()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Popular movies should default to 0")
        void newDatasetMetrics_ShouldHaveZeroPopularMovies() {
            DatasetMetrics newDatasetMetrics = new DatasetMetrics();
            assertThat(newDatasetMetrics.getPopularMovies()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Model version should default to 1.0.0")
        void newDatasetMetrics_ShouldHaveDefaultModelVersion() {
            DatasetMetrics newDatasetMetrics = new DatasetMetrics();
            assertThat(newDatasetMetrics.getModelVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Training status should default to PENDING")
        void newDatasetMetrics_ShouldHavePendingTrainingStatus() {
            DatasetMetrics newDatasetMetrics = new DatasetMetrics();
            assertThat(newDatasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.PENDING);
        }

        @Test
        @DisplayName("Sparsity precision should handle 6 decimal places")
        void sparsity_ShouldHandleSixDecimalPlaces() {
            BigDecimal preciseSparsity = new BigDecimal("0.123456");
            datasetMetrics.setSparsity(preciseSparsity);
            assertThat(datasetMetrics.getSparsity()).isEqualByComparingTo(preciseSparsity);
        }

        @Test
        @DisplayName("Training status can progress through lifecycle")
        void trainingStatus_CanProgressThroughLifecycle() {
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.PENDING);
            
            datasetMetrics.setTrainingStatus(DatasetMetrics.TrainingStatus.SCHEDULED);
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.SCHEDULED);
            
            datasetMetrics.setTrainingStatus(DatasetMetrics.TrainingStatus.IN_PROGRESS);
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.IN_PROGRESS);
            
            datasetMetrics.setTrainingStatus(DatasetMetrics.TrainingStatus.COMPLETED);
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.COMPLETED);
        }

        @Test
        @DisplayName("Training timing can be tracked")
        void trainingTiming_CanBeTracked() {
            LocalDateTime startTime = LocalDateTime.now();
            datasetMetrics.setLastTrainingStarted(startTime);
            assertThat(datasetMetrics.getLastTrainingStarted()).isEqualTo(startTime);
            
            LocalDateTime endTime = startTime.plusHours(2);
            datasetMetrics.setLastTrainingCompleted(endTime);
            assertThat(datasetMetrics.getLastTrainingCompleted()).isEqualTo(endTime);
        }

        @Test
        @DisplayName("Training errors can be captured")
        void trainingErrors_CanBeCaptured() {
            datasetMetrics.setTrainingStatus(DatasetMetrics.TrainingStatus.FAILED);
            datasetMetrics.setTrainingError("Insufficient memory for large matrix operations");
            
            assertThat(datasetMetrics.getTrainingStatus()).isEqualTo(DatasetMetrics.TrainingStatus.FAILED);
            assertThat(datasetMetrics.getTrainingError()).isEqualTo("Insufficient memory for large matrix operations");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero total users")
        void shouldHandleZeroTotalUsers() {
            datasetMetrics.setTotalUsers(0L);
            assertThat(datasetMetrics.getTotalUsers()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle zero total movies")
        void shouldHandleZeroTotalMovies() {
            datasetMetrics.setTotalMovies(0L);
            assertThat(datasetMetrics.getTotalMovies()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle zero total ratings")
        void shouldHandleZeroTotalRatings() {
            datasetMetrics.setTotalRatings(0L);
            assertThat(datasetMetrics.getTotalRatings()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            Long largeNumber = 1_000_000_000L;
            datasetMetrics.setTotalUsers(largeNumber);
            datasetMetrics.setTotalMovies(largeNumber);
            datasetMetrics.setTotalRatings(largeNumber);
            
            assertThat(datasetMetrics.getTotalUsers()).isEqualTo(largeNumber);
            assertThat(datasetMetrics.getTotalMovies()).isEqualTo(largeNumber);
            assertThat(datasetMetrics.getTotalRatings()).isEqualTo(largeNumber);
        }

        @Test
        @DisplayName("Should handle zero average values")
        void shouldHandleZeroAverageValues() {
            BigDecimal zero = BigDecimal.ZERO;
            datasetMetrics.setAvgRatingsPerUser(zero);
            datasetMetrics.setAvgRatingsPerMovie(zero);
            datasetMetrics.setAvgRatingValue(zero);
            
            assertThat(datasetMetrics.getAvgRatingsPerUser()).isEqualByComparingTo(zero);
            assertThat(datasetMetrics.getAvgRatingsPerMovie()).isEqualByComparingTo(zero);
            assertThat(datasetMetrics.getAvgRatingValue()).isEqualByComparingTo(zero);
        }

        @Test
        @DisplayName("Should handle empty model version")
        void shouldHandleEmptyModelVersion() {
            datasetMetrics.setModelVersion("");
            assertThat(datasetMetrics.getModelVersion()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty training error")
        void shouldHandleEmptyTrainingError() {
            datasetMetrics.setTrainingError("");
            assertThat(datasetMetrics.getTrainingError()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long training error")
        void shouldHandleVeryLongTrainingError() {
            String longError = "Error occurred during training process. ".repeat(100);
            datasetMetrics.setTrainingError(longError);
            assertThat(datasetMetrics.getTrainingError()).isEqualTo(longError);
        }

        @Test
        @DisplayName("Should handle same start and completion times")
        void shouldHandleSameStartAndCompletionTimes() {
            LocalDateTime timestamp = LocalDateTime.now();
            datasetMetrics.setLastTrainingStarted(timestamp);
            datasetMetrics.setLastTrainingCompleted(timestamp);
            
            assertThat(datasetMetrics.getLastTrainingStarted()).isEqualTo(timestamp);
            assertThat(datasetMetrics.getLastTrainingCompleted()).isEqualTo(timestamp);
        }
    }
}
