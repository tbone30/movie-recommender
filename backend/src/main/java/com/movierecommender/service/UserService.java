package com.movierecommender.service;

import com.movierecommender.entity.User;
import com.movierecommender.repository.UserRepository;
import com.movierecommender.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

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

    public User deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        return userRepository.save(user);
    }
}