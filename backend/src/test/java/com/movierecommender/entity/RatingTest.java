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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rating Entity Tests")
class RatingTest {

    private static Validator validator;
    private Rating rating;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        rating = new Rating();
        rating.setUserId(1L);
        rating.setMovieId(1L);
        rating.setRating(new BigDecimal("4.0"));
        rating.setWatchedDate(LocalDate.now());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create rating with default values")
        void defaultConstructor_ShouldCreateRatingWithDefaultValues() {
            Rating rating = new Rating();

            assertThat(rating.getId()).isNull();
            assertThat(rating.getUserId()).isNull();
            assertThat(rating.getMovieId()).isNull();
            assertThat(rating.getRating()).isNull();
            assertThat(rating.getReviewText()).isNull();
            assertThat(rating.getWatchedDate()).isNull();
            assertThat(rating.getIsRewatch()).isFalse();
            assertThat(rating.getIsPublic()).isTrue();
            assertThat(rating.getLikes()).isEqualTo(0);
            assertThat(rating.getLetterboxdReviewId()).isNull();
            assertThat(rating.getCreatedAt()).isNull();
            assertThat(rating.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Parameterized constructor should set required fields")
        void parameterizedConstructor_ShouldSetRequiredFields() {
            Long userId = 1L;
            Long movieId = 2L;
            BigDecimal ratingValue = new BigDecimal("3.5");
            LocalDate watchedDate = LocalDate.of(2023, 12, 25);

            Rating rating = new Rating(userId, movieId, ratingValue, watchedDate);

            assertThat(rating.getUserId()).isEqualTo(userId);
            assertThat(rating.getMovieId()).isEqualTo(movieId);
            assertThat(rating.getRating()).isEqualByComparingTo(ratingValue);
            assertThat(rating.getWatchedDate()).isEqualTo(watchedDate);
            assertThat(rating.getIsRewatch()).isFalse();
            assertThat(rating.getIsPublic()).isTrue();
            assertThat(rating.getLikes()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid rating should pass validation")
        void validRating_ShouldPassValidation() {
            Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
            assertThat(violations).isEmpty();
        }

        @Nested
        @DisplayName("User ID Validation")
        class UserIdValidationTests {

            @Test
            @DisplayName("Should reject null user ID")
            void nullUserId_ShouldFailValidation() {
                rating.setUserId(null);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid user ID")
            void validUserId_ShouldPassValidation() {
                rating.setUserId(123L);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Movie ID Validation")
        class MovieIdValidationTests {

            @Test
            @DisplayName("Should reject null movie ID")
            void nullMovieId_ShouldFailValidation() {
                rating.setMovieId(null);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid movie ID")
            void validMovieId_ShouldPassValidation() {
                rating.setMovieId(456L);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Rating Value Validation")
        class RatingValueValidationTests {

            @Test
            @DisplayName("Should reject null rating")
            void nullRating_ShouldFailValidation() {
                rating.setRating(null);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should reject rating below minimum")
            void ratingBelowMinimum_ShouldFailValidation() {
                rating.setRating(new BigDecimal("0.4"));
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("Rating must be at least 0.5");
            }

            @Test
            @DisplayName("Should reject rating above maximum")
            void ratingAboveMaximum_ShouldFailValidation() {
                rating.setRating(new BigDecimal("5.1"));
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("Rating must not exceed 5.0");
            }

            @Test
            @DisplayName("Should accept minimum rating")
            void minimumRating_ShouldPassValidation() {
                rating.setRating(new BigDecimal("0.5"));
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept maximum rating")
            void maximumRating_ShouldPassValidation() {
                rating.setRating(new BigDecimal("5.0"));
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid rating in range")
            void validRatingInRange_ShouldPassValidation() {
                rating.setRating(new BigDecimal("3.5"));
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Watched Date Validation")
        class WatchedDateValidationTests {

            @Test
            @DisplayName("Should reject null watched date")
            void nullWatchedDate_ShouldFailValidation() {
                rating.setWatchedDate(null);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be null");
            }

            @Test
            @DisplayName("Should accept valid watched date")
            void validWatchedDate_ShouldPassValidation() {
                rating.setWatchedDate(LocalDate.of(2023, 6, 15));
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Review Text Validation")
        class ReviewTextValidationTests {

            @Test
            @DisplayName("Should accept null review text")
            void nullReviewText_ShouldPassValidation() {
                rating.setReviewText(null);
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept any length review text (no size validation)")
            void anyLengthReviewText_ShouldPassValidation() {
                rating.setReviewText("a".repeat(3000)); // No @Size constraint
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid review text")
            void validReviewText_ShouldPassValidation() {
                rating.setReviewText("Great movie! Really enjoyed the plot twists and character development.");
                Set<ConstraintViolation<Rating>> violations = validator.validate(rating);
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
            rating.setId(id);
            assertThat(rating.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should get and set user ID")
        void shouldGetAndSetUserId() {
            Long userId = 456L;
            rating.setUserId(userId);
            assertThat(rating.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should get and set movie ID")
        void shouldGetAndSetMovieId() {
            Long movieId = 789L;
            rating.setMovieId(movieId);
            assertThat(rating.getMovieId()).isEqualTo(movieId);
        }

        @Test
        @DisplayName("Should get and set rating")
        void shouldGetAndSetRating() {
            BigDecimal ratingValue = new BigDecimal("4.5");
            rating.setRating(ratingValue);
            assertThat(rating.getRating()).isEqualByComparingTo(ratingValue);
        }

        @Test
        @DisplayName("Should get and set review text")
        void shouldGetAndSetReviewText() {
            String reviewText = "Excellent cinematography and acting!";
            rating.setReviewText(reviewText);
            assertThat(rating.getReviewText()).isEqualTo(reviewText);
        }

        @Test
        @DisplayName("Should get and set watched date")
        void shouldGetAndSetWatchedDate() {
            LocalDate watchedDate = LocalDate.of(2023, 7, 4);
            rating.setWatchedDate(watchedDate);
            assertThat(rating.getWatchedDate()).isEqualTo(watchedDate);
        }

        @Test
        @DisplayName("Should get and set is rewatch")
        void shouldGetAndSetIsRewatch() {
            rating.setIsRewatch(true);
            assertThat(rating.getIsRewatch()).isTrue();
            
            rating.setIsRewatch(false);
            assertThat(rating.getIsRewatch()).isFalse();
        }

        @Test
        @DisplayName("Should get and set is public")
        void shouldGetAndSetIsPublic() {
            rating.setIsPublic(false);
            assertThat(rating.getIsPublic()).isFalse();
            
            rating.setIsPublic(true);
            assertThat(rating.getIsPublic()).isTrue();
        }

        @Test
        @DisplayName("Should get and set likes")
        void shouldGetAndSetLikes() {
            Integer likes = 42;
            rating.setLikes(likes);
            assertThat(rating.getLikes()).isEqualTo(likes);
        }

        @Test
        @DisplayName("Should get and set letterboxd review ID")
        void shouldGetAndSetLetterboxdReviewId() {
            String letterboxdReviewId = "letterboxd-review-123";
            rating.setLetterboxdReviewId(letterboxdReviewId);
            assertThat(rating.getLetterboxdReviewId()).isEqualTo(letterboxdReviewId);
        }

        @Test
        @DisplayName("Should get and set created at")
        void shouldGetAndSetCreatedAt() {
            LocalDateTime createdAt = LocalDateTime.now();
            rating.setCreatedAt(createdAt);
            assertThat(rating.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should get and set updated at")
        void shouldGetAndSetUpdatedAt() {
            LocalDateTime updatedAt = LocalDateTime.now();
            rating.setUpdatedAt(updatedAt);
            assertThat(rating.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Is rewatch should default to false")
        void newRating_ShouldHaveIsRewatchFalse() {
            Rating newRating = new Rating();
            assertThat(newRating.getIsRewatch()).isFalse();
        }

        @Test
        @DisplayName("Is public should default to true")
        void newRating_ShouldHaveIsPublicTrue() {
            Rating newRating = new Rating();
            assertThat(newRating.getIsPublic()).isTrue();
        }

        @Test
        @DisplayName("Likes should default to 0")
        void newRating_ShouldHaveZeroLikes() {
            Rating newRating = new Rating();
            assertThat(newRating.getLikes()).isEqualTo(0);
        }

        @Test
        @DisplayName("Rating can be incremented by half points")
        void rating_CanBeIncrementedByHalfPoints() {
            rating.setRating(new BigDecimal("1.0"));
            assertThat(rating.getRating()).isEqualByComparingTo(new BigDecimal("1.0"));
            
            rating.setRating(new BigDecimal("1.5"));
            assertThat(rating.getRating()).isEqualByComparingTo(new BigDecimal("1.5"));
            
            rating.setRating(new BigDecimal("2.0"));
            assertThat(rating.getRating()).isEqualByComparingTo(new BigDecimal("2.0"));
        }

        @Test
        @DisplayName("Likes can be updated")
        void likes_CanBeUpdated() {
            assertThat(rating.getLikes()).isEqualTo(0);
            
            rating.setLikes(5);
            assertThat(rating.getLikes()).isEqualTo(5);
            
            rating.setLikes(10);
            assertThat(rating.getLikes()).isEqualTo(10);
        }

        @Test
        @DisplayName("Rating visibility can be toggled")
        void ratingVisibility_CanBeToggled() {
            assertThat(rating.getIsPublic()).isTrue();
            
            rating.setIsPublic(false);
            assertThat(rating.getIsPublic()).isFalse();
            
            rating.setIsPublic(true);
            assertThat(rating.getIsPublic()).isTrue();
        }

        @Test
        @DisplayName("Rewatch status can be toggled")
        void rewatchStatus_CanBeToggled() {
            assertThat(rating.getIsRewatch()).isFalse();
            
            rating.setIsRewatch(true);
            assertThat(rating.getIsRewatch()).isTrue();
            
            rating.setIsRewatch(false);
            assertThat(rating.getIsRewatch()).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle future watched dates")
        void shouldHandleFutureWatchedDates() {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            rating.setWatchedDate(futureDate);
            assertThat(rating.getWatchedDate()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("Should handle very old watched dates")
        void shouldHandleVeryOldWatchedDates() {
            LocalDate oldDate = LocalDate.of(1900, 1, 1);
            rating.setWatchedDate(oldDate);
            assertThat(rating.getWatchedDate()).isEqualTo(oldDate);
        }

        @Test
        @DisplayName("Should handle negative likes")
        void shouldHandleNegativeLikes() {
            rating.setLikes(-5);
            assertThat(rating.getLikes()).isEqualTo(-5);
        }

        @Test
        @DisplayName("Should handle very high likes")
        void shouldHandleVeryHighLikes() {
            Integer highLikes = 1_000_000;
            rating.setLikes(highLikes);
            assertThat(rating.getLikes()).isEqualTo(highLikes);
        }

        @Test
        @DisplayName("Should handle empty review text")
        void shouldHandleEmptyReviewText() {
            rating.setReviewText("");
            assertThat(rating.getReviewText()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long review text")
        void shouldHandleVeryLongReviewText() {
            String longReview = "Amazing movie! ".repeat(200);
            rating.setReviewText(longReview);
            assertThat(rating.getReviewText()).isEqualTo(longReview);
        }
    }
}
