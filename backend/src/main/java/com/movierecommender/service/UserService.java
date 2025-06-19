package com.movierecommender.service;

import com.movierecommender.entity.Rating;
import com.movierecommender.entity.User;
import com.movierecommender.repository.UserRepository;
import com.movierecommender.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return UserPrincipal.create(user);
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long userId, User userDetails) {
        User user = getUserById(userId);
        
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email is already in use!");
            }
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getLetterboxdUsername() != null) {
            if (userRepository.existsByLetterboxdUsername(userDetails.getLetterboxdUsername())) {
                throw new RuntimeException("Letterboxd username is already linked to another account!");
            }
            user.setLetterboxdUsername(userDetails.getLetterboxdUsername());
            user.setSyncStatus(User.SyncStatus.PENDING);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByLetterboxdUsername(String letterboxdUsername) {
        return userRepository.findByLetterboxdUsername(letterboxdUsername);
    }

    public User updateSyncStatus(Long userId, User.SyncStatus status) {
        User user = getUserById(userId);
        user.setSyncStatus(status);
        if (status == User.SyncStatus.COMPLETED) {
            user.setLastSyncDate(LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersNeedingSync(int hours) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(hours);
        return userRepository.findUsersNeedingSync(cutoffDate);
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsersWithLetterboxd() {
        return userRepository.findActiveUsersWithLetterboxd();
    }

    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User activateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        return userRepository.save(user);
    }

    public void syncUserWithLetterboxd(Long userId, String letterboxdUsername) {
        User user = getUserById(userId);
        user.setLetterboxdUsername(letterboxdUsername);
        user.setSyncStatus(User.SyncStatus.PENDING);
        userRepository.save(user);
        logger.info("User {} sync with Letterboxd initiated for username: {}", userId, letterboxdUsername);
    }

    @Transactional(readOnly = true)
    public Page<Rating> getUserRatings(Long userId, Pageable pageable) {
        // This would typically come from a RatingRepository
        // For now, return empty page since RatingService doesn't exist
        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = getUserById(userId);
        
        // Calculate basic statistics
        Map<String, Object> stats = Map.of(
            "user_id", userId,
            "username", user.getUsername(),
            "join_date", user.getCreatedAt(),
            "sync_status", user.getSyncStatus(),
            "last_sync_date", user.getLastSyncDate(),
            "total_ratings", 0, // Would come from RatingRepository
            "average_rating", 0.0, // Would come from RatingRepository
            "letterboxd_username", user.getLetterboxdUsername()
        );
        
        return stats;
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        logger.info("User {} marked as inactive (soft delete)", userId);
    }
}