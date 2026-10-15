package com.astroplatform.service;

import com.astroplatform.entity.SavedProfile;
import com.astroplatform.entity.User;
import com.astroplatform.repository.SavedProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedProfileService {

    private final SavedProfileRepository savedProfileRepository;

    public SavedProfileService(SavedProfileRepository savedProfileRepository) {
        this.savedProfileRepository = savedProfileRepository;
    }

    public List<SavedProfile> getAllProfiles() {
        return savedProfileRepository.findAll();
    }

    public SavedProfile saveProfile(SavedProfile profile, User user) {
        profile.setUser(user);
        return savedProfileRepository.save(profile);
    }

    // Verify ownership before updating
    public SavedProfile updateProfile(Long id, SavedProfile updatedData, Long userId) {
        return savedProfileRepository.findByIdAndUserId(id, userId).map(existingProfile -> {
            existingProfile.setName(updatedData.getName());
            existingProfile.setGender(updatedData.getGender());
            existingProfile.setYear(updatedData.getYear());
            existingProfile.setMonth(updatedData.getMonth());
            existingProfile.setDay(updatedData.getDay());
            existingProfile.setHour(updatedData.getHour());
            existingProfile.setMinute(updatedData.getMinute());
            existingProfile.setLocation(updatedData.getLocation());
            return savedProfileRepository.save(existingProfile);
        }).orElseThrow(() -> new RuntimeException("Profile not found or unauthorized"));
    }

    public void deleteProfile(Long id, Long userId) {
        SavedProfile profile = savedProfileRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Profile not found or unauthorized"));
        savedProfileRepository.delete(profile);
    }

    public List<SavedProfile> getUserProfiles(Long userId) {
        return savedProfileRepository.findByUserId(userId);
    }
}