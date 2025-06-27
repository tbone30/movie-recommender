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

@DisplayName("Recommendation Entity Tests")
class RecommendationTest {

    private static Validator validator;
    private Recommendation recommendation;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        recommendation = new Recommendation();
        recommendation.setUserId(1L);
        recommendation.setMovieId(1L);
        recommendation.setScore(new BigDecimal("0.8500"));
        recommendation.setAlgorithm("collaborative_filtering");
        recommendation.setRank(1);
        recommendation.setModelVersion("v1.0");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create recommendation with default values")
        void defaultConstructor_ShouldCreateRecommendationWithDefaultValues() {
            Recommendation recommendation = new Recommendation();

            assertThat(recommendation.getId()).isNull();
            assertThat(recommendation.getUserId()).isNull();
            assertThat(recommendation.getMovieId()).isNull();
            assertThat(recommendation.getScore()).isNull();
            assertThat(recommendation.getAlgorithm()).isNull();
            assertThat(recommendation.getExplanation()).isNull();
            assertThat(recommendation.getRank()).isNull();
            assertThat(recommendation.getModelVersion()).isNull();
            assertThat(recommendation.getCreatedAt()).isNull();
            assertThat(recommendation.getViewedAt()).isNull();
            assertThat(recommendation.getClickedAt()).isNull();
            assertThat(recommendation.getIsHidden()).isFalse();
        }

        @Test
        @DisplayName("Parameterized constructor should set required fields")
        void parameterizedConstructor_ShouldSetRequiredFields() {
            Long userId = 1L;
            Long movieId = 2L;
            BigDecimal score = new BigDecimal("0.7500");
            String algorithm = "matrix_factorization";
            Integer rank = 5;
            String modelVersion = "v2.1";

            Recommendation recommendation = new Recommendation(userId, movieId, score, algorithm, rank, modelVersion);

            assertThat(recommendation.getUserId()).isEqualTo(userId);
            assertThat(recommendation.getMovieId()).isEqualTo(movieId);
            assertThat(recommendation.getScore()).isEqualByComparingTo(score);
            assertThat(recommendation.getAlgorithm()).isEqualTo(algorithm);
            assertThat(recommendation.getRank()).isEqualTo(rank);
            assertThat(recommendation.getModelVersion()).isEqualTo(modelVersion);
            assertThat(recommendation.getIsHidden()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid recommendation should pass validation")
        void validRecommendation_ShouldPassValidation() {
            Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
            assertThat(violations).isEmpty();
        }

        @Nested
        @DisplayName("User ID Validation")
        class UserIdValidationTests {

            @Test
            @DisplayName("Should reject null user ID")
            void nullUserId_ShouldFailValidation() {
                recommendation.setUserId(null);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid user ID")
            void validUserId_ShouldPassValidation() {
                recommendation.setUserId(123L);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Movie ID Validation")
        class MovieIdValidationTests {

            @Test
            @DisplayName("Should reject null movie ID")
            void nullMovieId_ShouldFailValidation() {
                recommendation.setMovieId(null);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid movie ID")
            void validMovieId_ShouldPassValidation() {
                recommendation.setMovieId(456L);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Score Validation")
        class ScoreValidationTests {

            @Test
            @DisplayName("Should reject null score")
            void nullScore_ShouldFailValidation() {
                recommendation.setScore(null);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should reject score below minimum")
            void scoreBelowMinimum_ShouldFailValidation() {
                recommendation.setScore(new BigDecimal("-0.1"));
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("Score must be at least 0.0");
            }

            @Test
            @DisplayName("Should reject score above maximum")
            void scoreAboveMaximum_ShouldFailValidation() {
                recommendation.setScore(new BigDecimal("1.1"));
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("Score must not exceed 1.0");
            }

            @Test
            @DisplayName("Should accept minimum score")
            void minimumScore_ShouldPassValidation() {
                recommendation.setScore(new BigDecimal("0.0"));
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept maximum score")
            void maximumScore_ShouldPassValidation() {
                recommendation.setScore(new BigDecimal("1.0"));
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid score in range")
            void validScoreInRange_ShouldPassValidation() {
                recommendation.setScore(new BigDecimal("0.6789"));
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Algorithm Validation")
        class AlgorithmValidationTests {

            @Test
            @DisplayName("Should reject null algorithm")
            void nullAlgorithm_ShouldFailValidation() {
                recommendation.setAlgorithm(null);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid algorithm")
            void validAlgorithm_ShouldPassValidation() {
                recommendation.setAlgorithm("deep_learning");
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Explanation Validation")
        class ExplanationValidationTests {

            @Test
            @DisplayName("Should accept null explanation")
            void nullExplanation_ShouldPassValidation() {
                recommendation.setExplanation(null);
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept any length explanation (no size validation)")
            void anyLengthExplanation_ShouldPassValidation() {
                recommendation.setExplanation("a".repeat(1000)); // No @Size constraint
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid explanation")
            void validExplanation_ShouldPassValidation() {
                recommendation.setExplanation("Recommended because you liked similar sci-fi movies.");
                Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
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
            recommendation.setId(id);
            assertThat(recommendation.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should get and set user ID")
        void shouldGetAndSetUserId() {
            Long userId = 456L;
            recommendation.setUserId(userId);
            assertThat(recommendation.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should get and set movie ID")
        void shouldGetAndSetMovieId() {
            Long movieId = 789L;
            recommendation.setMovieId(movieId);
            assertThat(recommendation.getMovieId()).isEqualTo(movieId);
        }

        @Test
        @DisplayName("Should get and set score")
        void shouldGetAndSetScore() {
            BigDecimal score = new BigDecimal("0.9123");
            recommendation.setScore(score);
            assertThat(recommendation.getScore()).isEqualByComparingTo(score);
        }

        @Test
        @DisplayName("Should get and set algorithm")
        void shouldGetAndSetAlgorithm() {
            String algorithm = "neural_collaborative_filtering";
            recommendation.setAlgorithm(algorithm);
            assertThat(recommendation.getAlgorithm()).isEqualTo(algorithm);
        }

        @Test
        @DisplayName("Should get and set explanation")
        void shouldGetAndSetExplanation() {
            String explanation = "Based on your viewing history and similar users' preferences.";
            recommendation.setExplanation(explanation);
            assertThat(recommendation.getExplanation()).isEqualTo(explanation);
        }

        @Test
        @DisplayName("Should get and set rank")
        void shouldGetAndSetRank() {
            Integer rank = 10;
            recommendation.setRank(rank);
            assertThat(recommendation.getRank()).isEqualTo(rank);
        }

        @Test
        @DisplayName("Should get and set model version")
        void shouldGetAndSetModelVersion() {
            String modelVersion = "v3.2.1";
            recommendation.setModelVersion(modelVersion);
            assertThat(recommendation.getModelVersion()).isEqualTo(modelVersion);
        }

        @Test
        @DisplayName("Should get and set created at")
        void shouldGetAndSetCreatedAt() {
            LocalDateTime createdAt = LocalDateTime.now();
            recommendation.setCreatedAt(createdAt);
            assertThat(recommendation.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should get and set viewed at")
        void shouldGetAndSetViewedAt() {
            LocalDateTime viewedAt = LocalDateTime.now();
            recommendation.setViewedAt(viewedAt);
            assertThat(recommendation.getViewedAt()).isEqualTo(viewedAt);
        }

        @Test
        @DisplayName("Should get and set clicked at")
        void shouldGetAndSetClickedAt() {
            LocalDateTime clickedAt = LocalDateTime.now();
            recommendation.setClickedAt(clickedAt);
            assertThat(recommendation.getClickedAt()).isEqualTo(clickedAt);
        }

        @Test
        @DisplayName("Should get and set is hidden")
        void shouldGetAndSetIsHidden() {
            recommendation.setIsHidden(true);
            assertThat(recommendation.getIsHidden()).isTrue();
            
            recommendation.setIsHidden(false);
            assertThat(recommendation.getIsHidden()).isFalse();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Is hidden should default to false")
        void newRecommendation_ShouldHaveIsHiddenFalse() {
            Recommendation newRecommendation = new Recommendation();
            assertThat(newRecommendation.getIsHidden()).isFalse();
        }

        @Test
        @DisplayName("Score precision should handle 4 decimal places")
        void score_ShouldHandleFourDecimalPlaces() {
            BigDecimal preciseScore = new BigDecimal("0.1234");
            recommendation.setScore(preciseScore);
            assertThat(recommendation.getScore()).isEqualByComparingTo(preciseScore);
        }

        @Test
        @DisplayName("Recommendation visibility can be toggled")
        void recommendationVisibility_CanBeToggled() {
            assertThat(recommendation.getIsHidden()).isFalse();
            
            recommendation.setIsHidden(true);
            assertThat(recommendation.getIsHidden()).isTrue();
            
            recommendation.setIsHidden(false);
            assertThat(recommendation.getIsHidden()).isFalse();
        }

        @Test
        @DisplayName("Ranking can be updated")
        void ranking_CanBeUpdated() {
            recommendation.setRank(1);
            assertThat(recommendation.getRank()).isEqualTo(1);
            
            recommendation.setRank(5);
            assertThat(recommendation.getRank()).isEqualTo(5);
            
            recommendation.setRank(100);
            assertThat(recommendation.getRank()).isEqualTo(100);
        }

        @Test
        @DisplayName("User interaction timestamps can be tracked")
        void userInteractionTimestamps_CanBeTracked() {
            LocalDateTime now = LocalDateTime.now();
            
            recommendation.setViewedAt(now);
            assertThat(recommendation.getViewedAt()).isEqualTo(now);
            
            LocalDateTime clickTime = now.plusMinutes(5);
            recommendation.setClickedAt(clickTime);
            assertThat(recommendation.getClickedAt()).isEqualTo(clickTime);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero rank")
        void shouldHandleZeroRank() {
            recommendation.setRank(0);
            assertThat(recommendation.getRank()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle negative rank")
        void shouldHandleNegativeRank() {
            recommendation.setRank(-1);
            assertThat(recommendation.getRank()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle very high rank")
        void shouldHandleVeryHighRank() {
            Integer highRank = 1_000_000;
            recommendation.setRank(highRank);
            assertThat(recommendation.getRank()).isEqualTo(highRank);
        }

        @Test
        @DisplayName("Should handle empty algorithm")
        void shouldHandleEmptyAlgorithm() {
            recommendation.setAlgorithm("");
            assertThat(recommendation.getAlgorithm()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty model version")
        void shouldHandleEmptyModelVersion() {
            recommendation.setModelVersion("");
            assertThat(recommendation.getModelVersion()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty explanation")
        void shouldHandleEmptyExplanation() {
            recommendation.setExplanation("");
            assertThat(recommendation.getExplanation()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long explanation")
        void shouldHandleVeryLongExplanation() {
            String longExplanation = "This recommendation is based on your preferences. ".repeat(50);
            recommendation.setExplanation(longExplanation);
            assertThat(recommendation.getExplanation()).isEqualTo(longExplanation);
        }

        @Test
        @DisplayName("Should handle same viewed and clicked timestamps")
        void shouldHandleSameViewedAndClickedTimestamps() {
            LocalDateTime timestamp = LocalDateTime.now();
            recommendation.setViewedAt(timestamp);
            recommendation.setClickedAt(timestamp);
            assertThat(recommendation.getViewedAt()).isEqualTo(timestamp);
            assertThat(recommendation.getClickedAt()).isEqualTo(timestamp);
        }
    }
}
