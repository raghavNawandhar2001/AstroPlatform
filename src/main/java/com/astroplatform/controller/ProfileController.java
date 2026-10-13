package com.astroplatform.controller;

import com.astroplatform.entity.SavedProfile;
import com.astroplatform.service.SavedProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*") // Ensure this matches your Vite/React port!
public class ProfileController {

    private final SavedProfileService savedProfileService;

    public ProfileController(SavedProfileService savedProfileService) {
        this.savedProfileService = savedProfileService;
    }

    // GET: Fetch all saved profiles
    @GetMapping
    public ResponseEntity<List<SavedProfile>> getAllProfiles() {
        return ResponseEntity.ok(savedProfileService.getAllProfiles());
    }

    // POST: Create a new profile
    @PostMapping
    public ResponseEntity<SavedProfile> createProfile(@RequestBody SavedProfile profile) {
        return ResponseEntity.ok(savedProfileService.saveProfile(profile));
    }

    // PUT: Update an existing profile
    @PutMapping("/{id}")
    public ResponseEntity<SavedProfile> updateProfile(@PathVariable Long id, @RequestBody SavedProfile profile) {
        return ResponseEntity.ok(savedProfileService.updateProfile(id, profile));
    }

    // DELETE: Remove a profile
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        savedProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}