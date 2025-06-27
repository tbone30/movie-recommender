package com.movierecommender.controller;

import com.movierecommender.entity.Rating;
import com.movierecommender.entity.User;
import com.movierecommender.service.UserService;
import com.movierecommender.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserService userService;    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userService.getUserById(userPrincipal.getId());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> updates) {
        try {
            User userDetails = new User();
            if (updates.get("email") != null) {
                userDetails.setEmail(updates.get("email"));
            }
            if (updates.get("letterboxd_username") != null) {
                userDetails.setLetterboxdUsername(updates.get("letterboxd_username"));
            }
            
            User user = userService.updateUser(userPrincipal.getId(), userDetails);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully", "user", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/letterboxd/sync")
    public ResponseEntity<?> syncLetterboxdData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> requestBody) {
        try {
            String letterboxdUsername = requestBody.get("letterboxd_username");
            if (letterboxdUsername == null || letterboxdUsername.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Letterboxd username is required"));
            }

            userService.syncUserWithLetterboxd(userPrincipal.getId(), letterboxdUsername);
            return ResponseEntity.ok(Map.of("message", "Letterboxd sync initiated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/letterboxd/status")
    public ResponseEntity<Map<String, Object>> getLetterboxdSyncStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userService.getUserById(userPrincipal.getId());
            return ResponseEntity.ok(Map.of(
                "letterboxd_username", user.getLetterboxdUsername(),
                "sync_status", user.getSyncStatus(),
                "last_sync_date", user.getLastSyncDate()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }    @GetMapping("/ratings")
    public ResponseEntity<Page<Rating>> getUserRatings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Rating> ratings = userService.getUserRatings(userPrincipal.getId(), pageable);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Page.empty());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Map<String, Object> stats = userService.getUserStatistics(userPrincipal.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            userService.deleteUser(userPrincipal.getId());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}