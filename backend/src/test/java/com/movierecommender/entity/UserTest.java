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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Tests")
class UserTest {

    private static Validator validator;
    private User user;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create user with default values")
        void defaultConstructor_ShouldCreateUserWithDefaultValues() {
            User user = new User();

            assertThat(user.getId()).isNull();
            assertThat(user.getUsername()).isNull();
            assertThat(user.getEmail()).isNull();
            assertThat(user.getPassword()).isNull();
            assertThat(user.getLetterboxdUsername()).isNull();
            assertThat(user.getCreatedAt()).isNull();
            assertThat(user.getUpdatedAt()).isNull();
            assertThat(user.getLastSyncDate()).isNull();
            assertThat(user.getSyncStatus()).isEqualTo(User.SyncStatus.PENDING);
            assertThat(user.getRoles()).isNotNull().isEmpty();
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Parameterized constructor should set required fields and default role")
        void parameterizedConstructor_ShouldSetRequiredFieldsAndDefaultRole() {
            User user = new User("testuser", "test@example.com", "password123");

            assertThat(user.getUsername()).isEqualTo("testuser");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isEqualTo("password123");
            assertThat(user.getRoles()).containsExactly(User.Role.USER);
            assertThat(user.getSyncStatus()).isEqualTo(User.SyncStatus.PENDING);
            assertThat(user.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid user should pass validation")
        void validUser_ShouldPassValidation() {
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).isEmpty();
        }

        @Nested
        @DisplayName("Username Validation")
        class UsernameValidationTests {

            @Test
            @DisplayName("Should reject null username")
            void nullUsername_ShouldFailValidation() {
                user.setUsername(null);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }            @Test
            @DisplayName("Should reject empty username")
            void emptyUsername_ShouldFailValidation() {
                user.setUsername("");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(2);
                assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("must not be blank"))).isTrue();
                assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("size must be between 3 and 50"))).isTrue();
            }

            @Test
            @DisplayName("Should reject blank username")
            void blankUsername_ShouldFailValidation() {
                user.setUsername("   ");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }

            @Test
            @DisplayName("Should reject username shorter than 3 characters")
            void shortUsername_ShouldFailValidation() {
                user.setUsername("ab");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 3 and 50");
            }

            @Test
            @DisplayName("Should reject username longer than 50 characters")
            void longUsername_ShouldFailValidation() {
                user.setUsername("a".repeat(51));
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 3 and 50");
            }

            @Test
            @DisplayName("Should accept username with minimum length")
            void minLengthUsername_ShouldPassValidation() {
                user.setUsername("abc");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should accept username with maximum length")
            void maxLengthUsername_ShouldPassValidation() {
                user.setUsername("a".repeat(50));
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Email Validation")
        class EmailValidationTests {

            @Test
            @DisplayName("Should reject null email")
            void nullEmail_ShouldFailValidation() {
                user.setEmail(null);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }

            @Test
            @DisplayName("Should reject empty email")
            void emptyEmail_ShouldFailValidation() {
                user.setEmail("");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }

            @Test
            @DisplayName("Should reject invalid email format")
            void invalidEmailFormat_ShouldFailValidation() {
                user.setEmail("invalid-email");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must be a well-formed email address");
            }

            @Test
            @DisplayName("Should accept valid email format")
            void validEmailFormat_ShouldPassValidation() {
                user.setEmail("user@example.com");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Password Validation")
        class PasswordValidationTests {

            @Test
            @DisplayName("Should reject null password")
            void nullPassword_ShouldFailValidation() {
                user.setPassword(null);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
            }            @Test
            @DisplayName("Should reject empty password")
            void emptyPassword_ShouldFailValidation() {
                user.setPassword("");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(2);
                assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("must not be blank"))).isTrue();
                assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("size must be between 8 and"))).isTrue();
            }            @Test
            @DisplayName("Should reject password shorter than 8 characters")
            void shortPassword_ShouldFailValidation() {
                user.setPassword("1234567");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 8 and");
            }            @Test
            @DisplayName("Should accept password with minimum length")
            void minLengthPassword_ShouldPassValidation() {
                user.setPassword("12345678");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Letterboxd Username Validation")
        class LetterboxdUsernameValidationTests {

            @Test
            @DisplayName("Should accept null letterboxd username")
            void nullLetterboxdUsername_ShouldPassValidation() {
                user.setLetterboxdUsername(null);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should reject letterboxd username shorter than 2 characters")
            void shortLetterboxdUsername_ShouldFailValidation() {
                user.setLetterboxdUsername("a");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 2 and 50");
            }

            @Test
            @DisplayName("Should reject letterboxd username longer than 50 characters")
            void longLetterboxdUsername_ShouldFailValidation() {
                user.setLetterboxdUsername("a".repeat(51));
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage()).contains("size must be between 2 and 50");
            }

            @Test
            @DisplayName("Should accept valid letterboxd username")
            void validLetterboxdUsername_ShouldPassValidation() {
                user.setLetterboxdUsername("letterboxduser");
                Set<ConstraintViolation<User>> violations = validator.validate(user);
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
            user.setId(id);
            assertThat(user.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should get and set username")
        void shouldGetAndSetUsername() {
            String username = "newusername";
            user.setUsername(username);
            assertThat(user.getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should get and set email")
        void shouldGetAndSetEmail() {
            String email = "newemail@example.com";
            user.setEmail(email);
            assertThat(user.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should get and set password")
        void shouldGetAndSetPassword() {
            String password = "newpassword123";
            user.setPassword(password);
            assertThat(user.getPassword()).isEqualTo(password);
        }

        @Test
        @DisplayName("Should get and set letterboxd username")
        void shouldGetAndSetLetterboxdUsername() {
            String letterboxdUsername = "letterboxduser";
            user.setLetterboxdUsername(letterboxdUsername);
            assertThat(user.getLetterboxdUsername()).isEqualTo(letterboxdUsername);
        }

        @Test
        @DisplayName("Should get and set created at")
        void shouldGetAndSetCreatedAt() {
            LocalDateTime createdAt = LocalDateTime.now();
            user.setCreatedAt(createdAt);
            assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should get and set updated at")
        void shouldGetAndSetUpdatedAt() {
            LocalDateTime updatedAt = LocalDateTime.now();
            user.setUpdatedAt(updatedAt);
            assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should get and set last sync date")
        void shouldGetAndSetLastSyncDate() {
            LocalDateTime lastSyncDate = LocalDateTime.now();
            user.setLastSyncDate(lastSyncDate);
            assertThat(user.getLastSyncDate()).isEqualTo(lastSyncDate);
        }

        @Test
        @DisplayName("Should get and set sync status")
        void shouldGetAndSetSyncStatus() {
            User.SyncStatus syncStatus = User.SyncStatus.COMPLETED;
            user.setSyncStatus(syncStatus);
            assertThat(user.getSyncStatus()).isEqualTo(syncStatus);
        }

        @Test
        @DisplayName("Should get and set roles")
        void shouldGetAndSetRoles() {
            Set<User.Role> roles = Set.of(User.Role.USER, User.Role.ADMIN);
            user.setRoles(roles);
            assertThat(user.getRoles()).isEqualTo(roles);
        }

        @Test
        @DisplayName("Should get and set is active")
        void shouldGetAndSetIsActive() {
            user.setIsActive(false);
            assertThat(user.getIsActive()).isFalse();
            
            user.setIsActive(true);
            assertThat(user.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("SyncStatus enum should have all expected values")
        void syncStatusEnum_ShouldHaveAllExpectedValues() {
            User.SyncStatus[] expectedValues = {
                User.SyncStatus.PENDING,
                User.SyncStatus.IN_PROGRESS,
                User.SyncStatus.COMPLETED,
                User.SyncStatus.FAILED,
                User.SyncStatus.PAUSED
            };
            
            assertThat(User.SyncStatus.values()).containsExactly(expectedValues);
        }

        @Test
        @DisplayName("Role enum should have all expected values")
        void roleEnum_ShouldHaveAllExpectedValues() {
            User.Role[] expectedValues = {
                User.Role.USER,
                User.Role.ADMIN
            };
            
            assertThat(User.Role.values()).containsExactly(expectedValues);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("New user should have USER role by default when using parameterized constructor")
        void newUser_ShouldHaveUserRoleByDefault() {
            User newUser = new User("testuser", "test@example.com", "password123");
            assertThat(newUser.getRoles()).containsExactly(User.Role.USER);
        }

        @Test
        @DisplayName("User should be active by default")
        void newUser_ShouldBeActiveByDefault() {
            User newUser = new User();
            assertThat(newUser.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("User should have PENDING sync status by default")
        void newUser_ShouldHavePendingSyncStatusByDefault() {
            User newUser = new User();
            assertThat(newUser.getSyncStatus()).isEqualTo(User.SyncStatus.PENDING);
        }

        @Test
        @DisplayName("User roles should be modifiable")
        void userRoles_ShouldBeModifiable() {
            Set<User.Role> roles = new HashSet<>();
            roles.add(User.Role.USER);
            roles.add(User.Role.ADMIN);
            
            user.setRoles(roles);
            assertThat(user.getRoles()).containsExactlyInAnyOrder(User.Role.USER, User.Role.ADMIN);
            
            user.getRoles().remove(User.Role.ADMIN);
            assertThat(user.getRoles()).containsExactly(User.Role.USER);
        }

        @Test
        @DisplayName("User can have multiple roles")
        void user_CanHaveMultipleRoles() {
            user.setRoles(Set.of(User.Role.USER, User.Role.ADMIN));
            assertThat(user.getRoles()).hasSize(2);
            assertThat(user.getRoles()).containsExactlyInAnyOrder(User.Role.USER, User.Role.ADMIN);
        }

        @Test
        @DisplayName("User sync status can be changed")
        void userSyncStatus_CanBeChanged() {
            assertThat(user.getSyncStatus()).isEqualTo(User.SyncStatus.PENDING);
            
            user.setSyncStatus(User.SyncStatus.IN_PROGRESS);
            assertThat(user.getSyncStatus()).isEqualTo(User.SyncStatus.IN_PROGRESS);
            
            user.setSyncStatus(User.SyncStatus.COMPLETED);
            assertThat(user.getSyncStatus()).isEqualTo(User.SyncStatus.COMPLETED);
        }
    }
}
