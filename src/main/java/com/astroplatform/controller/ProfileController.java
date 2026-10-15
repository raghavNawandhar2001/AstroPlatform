package com.astroplatform.controller;

import com.astroplatform.entity.SavedProfile;
import com.astroplatform.entity.User;
import com.astroplatform.repository.UserRepository;
import com.astroplatform.service.SavedProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "${frontend.url}")
public class ProfileController {

    private final SavedProfileService savedProfileService;
    private final UserRepository userRepository;

    public ProfileController(SavedProfileService savedProfileService, UserRepository userRepository) {
        this.savedProfileService = savedProfileService;
        this.userRepository = userRepository;
    }

    // Helper method to get the currently authenticated user from the JWT Token
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

    @GetMapping
    public ResponseEntity<List<SavedProfile>> getAllProfiles() {
        User currentUser = getAuthenticatedUser();
        return ResponseEntity.ok(savedProfileService.getUserProfiles(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<SavedProfile> createProfile(@RequestBody SavedProfile profile) {
        User currentUser = getAuthenticatedUser();
        return ResponseEntity.ok(savedProfileService.saveProfile(profile, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavedProfile> updateProfile(@PathVariable Long id, @RequestBody SavedProfile profile) {
        User currentUser = getAuthenticatedUser();
        return ResponseEntity.ok(savedProfileService.updateProfile(id, profile, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        User currentUser = getAuthenticatedUser();
        savedProfileService.deleteProfile(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}