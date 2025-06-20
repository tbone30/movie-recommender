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
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Movie Entity Tests")
class MovieTest {

    private static Validator validator;
    private Movie movie;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setYear(2023);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create movie with default values")
        void defaultConstructor_ShouldCreateMovieWithDefaultValues() {
            Movie movie = new Movie();

            assertThat(movie.getId()).isNull();
            assertThat(movie.getTmdbId()).isNull();
            assertThat(movie.getLetterboxdId()).isNull();
            assertThat(movie.getTitle()).isNull();
            assertThat(movie.getYear()).isNull();
            assertThat(movie.getGenres()).isNotNull().isEmpty();
            assertThat(movie.getDirector()).isNull();
            assertThat(movie.getActors()).isNotNull().isEmpty();
            assertThat(movie.getPosterUrl()).isNull();
            assertThat(movie.getAvgRating()).isNull();
            assertThat(movie.getRatingCount()).isEqualTo(0L);
            assertThat(movie.getOverview()).isNull();
            assertThat(movie.getRuntime()).isNull();
            assertThat(movie.getCountries()).isNotNull().isEmpty();
            assertThat(movie.getLanguages()).isNotNull().isEmpty();
            assertThat(movie.getBudget()).isNull();
            assertThat(movie.getRevenue()).isNull();
            assertThat(movie.getCreatedAt()).isNull();
            assertThat(movie.getUpdatedAt()).isNull();
            assertThat(movie.getLastMetadataUpdate()).isNull();
        }

        @Test
        @DisplayName("Parameterized constructor should set title and year")
        void parameterizedConstructor_ShouldSetTitleAndYear() {
            Movie movie = new Movie("The Matrix", 1999);

            assertThat(movie.getTitle()).isEqualTo("The Matrix");
            assertThat(movie.getYear()).isEqualTo(1999);
            assertThat(movie.getRatingCount()).isEqualTo(0L);
            assertThat(movie.getGenres()).isNotNull().isEmpty();
            assertThat(movie.getActors()).isNotNull().isEmpty();
            assertThat(movie.getCountries()).isNotNull().isEmpty();
            assertThat(movie.getLanguages()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid movie should pass validation")
        void validMovie_ShouldPassValidation() {
            Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
            assertThat(violations).isEmpty();
        }

        @Nested
        @DisplayName("Title Validation")
        class TitleValidationTests {

            @Test
            @DisplayName("Should reject null title")
            void nullTitle_ShouldFailValidation() {
                movie.setTitle(null);
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }

            @Test
            @DisplayName("Should reject empty title")
            void emptyTitle_ShouldFailValidation() {
                movie.setTitle("");
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }

            @Test
            @DisplayName("Should reject blank title")
            void blankTitle_ShouldFailValidation() {
                movie.setTitle("   ");
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }

            @Test
            @DisplayName("Should reject title longer than 255 characters")
            void longTitle_ShouldFailValidation() {
                movie.setTitle("a".repeat(256));
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 0 and 255");
            }

            @Test
            @DisplayName("Should accept title with maximum length")
            void maxLengthTitle_ShouldPassValidation() {
                movie.setTitle("a".repeat(255));
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid title")
            void validTitle_ShouldPassValidation() {
                movie.setTitle("The Shawshank Redemption");
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Director Validation")
        class DirectorValidationTests {

            @Test
            @DisplayName("Should accept null director")
            void nullDirector_ShouldPassValidation() {
                movie.setDirector(null);
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should reject director longer than 255 characters")
            void longDirector_ShouldFailValidation() {
                movie.setDirector("a".repeat(256));
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 0 and 255");
            }

            @Test
            @DisplayName("Should accept valid director")
            void validDirector_ShouldPassValidation() {
                movie.setDirector("Christopher Nolan");
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }
        }        @Nested
        @DisplayName("Poster URL Validation")
        class PosterUrlValidationTests {

            @Test
            @DisplayName("Should accept null poster URL")
            void nullPosterUrl_ShouldPassValidation() {
                movie.setPosterUrl(null);
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept any length poster URL (no size validation)")
            void anyLengthPosterUrl_ShouldPassValidation() {
                movie.setPosterUrl("a".repeat(1000)); // No @Size constraint
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid poster URL")
            void validPosterUrl_ShouldPassValidation() {
                movie.setPosterUrl("https://example.com/poster.jpg");
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }
        }        @Nested
        @DisplayName("Overview Validation")
        class OverviewValidationTests {

            @Test
            @DisplayName("Should accept null overview")
            void nullOverview_ShouldPassValidation() {
                movie.setOverview(null);
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept any length overview (no size validation)")
            void anyLengthOverview_ShouldPassValidation() {
                movie.setOverview("a".repeat(3000)); // No @Size constraint
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept valid overview")
            void validOverview_ShouldPassValidation() {
                movie.setOverview("A great movie about redemption and hope.");
                Set<ConstraintViolation<Movie>> violations = validator.validate(movie);
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
            movie.setId(id);
            assertThat(movie.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should get and set TMDB id")
        void shouldGetAndSetTmdbId() {
            Long tmdbId = 550L;
            movie.setTmdbId(tmdbId);
            assertThat(movie.getTmdbId()).isEqualTo(tmdbId);
        }

        @Test
        @DisplayName("Should get and set Letterboxd id")
        void shouldGetAndSetLetterboxdId() {
            String letterboxdId = "fight-club";
            movie.setLetterboxdId(letterboxdId);
            assertThat(movie.getLetterboxdId()).isEqualTo(letterboxdId);
        }

        @Test
        @DisplayName("Should get and set title")
        void shouldGetAndSetTitle() {
            String title = "Fight Club";
            movie.setTitle(title);
            assertThat(movie.getTitle()).isEqualTo(title);
        }

        @Test
        @DisplayName("Should get and set year")
        void shouldGetAndSetYear() {
            Integer year = 1999;
            movie.setYear(year);
            assertThat(movie.getYear()).isEqualTo(year);
        }

        @Test
        @DisplayName("Should get and set genres")
        void shouldGetAndSetGenres() {
            Set<String> genres = Set.of("Drama", "Thriller");
            movie.setGenres(genres);
            assertThat(movie.getGenres()).isEqualTo(genres);
        }

        @Test
        @DisplayName("Should get and set director")
        void shouldGetAndSetDirector() {
            String director = "David Fincher";
            movie.setDirector(director);
            assertThat(movie.getDirector()).isEqualTo(director);
        }

        @Test
        @DisplayName("Should get and set actors")
        void shouldGetAndSetActors() {
            Set<String> actors = Set.of("Brad Pitt", "Edward Norton");
            movie.setActors(actors);
            assertThat(movie.getActors()).isEqualTo(actors);
        }

        @Test
        @DisplayName("Should get and set poster URL")
        void shouldGetAndSetPosterUrl() {
            String posterUrl = "https://example.com/poster.jpg";
            movie.setPosterUrl(posterUrl);
            assertThat(movie.getPosterUrl()).isEqualTo(posterUrl);
        }

        @Test
        @DisplayName("Should get and set average rating")
        void shouldGetAndSetAvgRating() {
            BigDecimal avgRating = new BigDecimal("8.5");
            movie.setAvgRating(avgRating);
            assertThat(movie.getAvgRating()).isEqualTo(avgRating);
        }

        @Test
        @DisplayName("Should get and set rating count")
        void shouldGetAndSetRatingCount() {
            Long ratingCount = 1000L;
            movie.setRatingCount(ratingCount);
            assertThat(movie.getRatingCount()).isEqualTo(ratingCount);
        }

        @Test
        @DisplayName("Should get and set overview")
        void shouldGetAndSetOverview() {
            String overview = "A great movie about inner conflict.";
            movie.setOverview(overview);
            assertThat(movie.getOverview()).isEqualTo(overview);
        }

        @Test
        @DisplayName("Should get and set runtime")
        void shouldGetAndSetRuntime() {
            Integer runtime = 139;
            movie.setRuntime(runtime);
            assertThat(movie.getRuntime()).isEqualTo(runtime);
        }

        @Test
        @DisplayName("Should get and set countries")
        void shouldGetAndSetCountries() {
            Set<String> countries = Set.of("USA", "Germany");
            movie.setCountries(countries);
            assertThat(movie.getCountries()).isEqualTo(countries);
        }

        @Test
        @DisplayName("Should get and set languages")
        void shouldGetAndSetLanguages() {
            Set<String> languages = Set.of("English", "German");
            movie.setLanguages(languages);
            assertThat(movie.getLanguages()).isEqualTo(languages);
        }

        @Test
        @DisplayName("Should get and set budget")
        void shouldGetAndSetBudget() {
            Long budget = 63000000L;
            movie.setBudget(budget);
            assertThat(movie.getBudget()).isEqualTo(budget);
        }

        @Test
        @DisplayName("Should get and set revenue")
        void shouldGetAndSetRevenue() {
            Long revenue = 100853753L;
            movie.setRevenue(revenue);
            assertThat(movie.getRevenue()).isEqualTo(revenue);
        }

        @Test
        @DisplayName("Should get and set created at")
        void shouldGetAndSetCreatedAt() {
            LocalDateTime createdAt = LocalDateTime.now();
            movie.setCreatedAt(createdAt);
            assertThat(movie.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should get and set updated at")
        void shouldGetAndSetUpdatedAt() {
            LocalDateTime updatedAt = LocalDateTime.now();
            movie.setUpdatedAt(updatedAt);
            assertThat(movie.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should get and set last metadata update")
        void shouldGetAndSetLastMetadataUpdate() {
            LocalDateTime lastMetadataUpdate = LocalDateTime.now();
            movie.setLastMetadataUpdate(lastMetadataUpdate);
            assertThat(movie.getLastMetadataUpdate()).isEqualTo(lastMetadataUpdate);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Rating count should default to 0")
        void newMovie_ShouldHaveZeroRatingCount() {
            Movie newMovie = new Movie();
            assertThat(newMovie.getRatingCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Collections should be initialized as empty sets")
        void newMovie_ShouldHaveEmptyCollections() {
            Movie newMovie = new Movie();
            assertThat(newMovie.getGenres()).isNotNull().isEmpty();
            assertThat(newMovie.getActors()).isNotNull().isEmpty();
            assertThat(newMovie.getCountries()).isNotNull().isEmpty();
            assertThat(newMovie.getLanguages()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Movie collections should be modifiable")
        void movieCollections_ShouldBeModifiable() {
            Set<String> genres = new HashSet<>();
            genres.add("Action");
            genres.add("Drama");
            
            movie.setGenres(genres);
            assertThat(movie.getGenres()).containsExactlyInAnyOrder("Action", "Drama");
            
            movie.getGenres().add("Thriller");
            assertThat(movie.getGenres()).containsExactlyInAnyOrder("Action", "Drama", "Thriller");
            
            movie.getGenres().remove("Action");
            assertThat(movie.getGenres()).containsExactlyInAnyOrder("Drama", "Thriller");
        }

        @Test
        @DisplayName("Movie can have multiple actors")
        void movie_CanHaveMultipleActors() {
            movie.setActors(Set.of("Actor 1", "Actor 2", "Actor 3"));
            assertThat(movie.getActors()).hasSize(3);
            assertThat(movie.getActors()).containsExactlyInAnyOrder("Actor 1", "Actor 2", "Actor 3");
        }

        @Test
        @DisplayName("Movie can have multiple countries")
        void movie_CanHaveMultipleCountries() {
            movie.setCountries(Set.of("USA", "UK", "Canada"));
            assertThat(movie.getCountries()).hasSize(3);
            assertThat(movie.getCountries()).containsExactlyInAnyOrder("USA", "UK", "Canada");
        }

        @Test
        @DisplayName("Movie can have multiple languages")
        void movie_CanHaveMultipleLanguages() {
            movie.setLanguages(Set.of("English", "French", "Spanish"));
            assertThat(movie.getLanguages()).hasSize(3);
            assertThat(movie.getLanguages()).containsExactlyInAnyOrder("English", "French", "Spanish");
        }

        @Test
        @DisplayName("Movie can have multiple genres")
        void movie_CanHaveMultipleGenres() {
            movie.setGenres(Set.of("Action", "Adventure", "Sci-Fi"));
            assertThat(movie.getGenres()).hasSize(3);
            assertThat(movie.getGenres()).containsExactlyInAnyOrder("Action", "Adventure", "Sci-Fi");
        }

        @Test
        @DisplayName("Average rating should handle decimal precision")
        void avgRating_ShouldHandleDecimalPrecision() {
            BigDecimal rating = new BigDecimal("8.75");
            movie.setAvgRating(rating);
            assertThat(movie.getAvgRating()).isEqualByComparingTo(rating);
        }

        @Test
        @DisplayName("Rating count can be updated")
        void ratingCount_CanBeUpdated() {
            assertThat(movie.getRatingCount()).isEqualTo(0L);
            
            movie.setRatingCount(100L);
            assertThat(movie.getRatingCount()).isEqualTo(100L);
            
            movie.setRatingCount(1000L);
            assertThat(movie.getRatingCount()).isEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very old movies")
        void shouldHandleVeryOldMovies() {
            movie.setYear(1900);
            assertThat(movie.getYear()).isEqualTo(1900);
        }

        @Test
        @DisplayName("Should handle future movies")
        void shouldHandleFutureMovies() {
            movie.setYear(2030);
            assertThat(movie.getYear()).isEqualTo(2030);
        }

        @Test
        @DisplayName("Should handle zero runtime")
        void shouldHandleZeroRuntime() {
            movie.setRuntime(0);
            assertThat(movie.getRuntime()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle very long runtime")
        void shouldHandleVeryLongRuntime() {
            movie.setRuntime(600); // 10 hours
            assertThat(movie.getRuntime()).isEqualTo(600);
        }

        @Test
        @DisplayName("Should handle zero budget")
        void shouldHandleZeroBudget() {
            movie.setBudget(0L);
            assertThat(movie.getBudget()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle zero revenue")
        void shouldHandleZeroRevenue() {
            movie.setRevenue(0L);
            assertThat(movie.getRevenue()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle very high budget")
        void shouldHandleVeryHighBudget() {
            Long highBudget = 1_000_000_000L; // 1 billion
            movie.setBudget(highBudget);
            assertThat(movie.getBudget()).isEqualTo(highBudget);
        }

        @Test
        @DisplayName("Should handle very high revenue")
        void shouldHandleVeryHighRevenue() {
            Long highRevenue = 2_000_000_000L; // 2 billion
            movie.setRevenue(highRevenue);
            assertThat(movie.getRevenue()).isEqualTo(highRevenue);
        }

        @Test
        @DisplayName("Should handle minimum average rating")
        void shouldHandleMinimumAvgRating() {
            BigDecimal minRating = new BigDecimal("0.00");
            movie.setAvgRating(minRating);
            assertThat(movie.getAvgRating()).isEqualByComparingTo(minRating);
        }

        @Test
        @DisplayName("Should handle maximum average rating")
        void shouldHandleMaximumAvgRating() {
            BigDecimal maxRating = new BigDecimal("10.00");
            movie.setAvgRating(maxRating);
            assertThat(movie.getAvgRating()).isEqualByComparingTo(maxRating);
        }
    }
}
