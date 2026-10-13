package com.astroplatform.service;

import com.astroplatform.entity.SavedProfile;
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

    public SavedProfile saveProfile(SavedProfile profile) {
        return savedProfileRepository.save(profile);
    }

    public SavedProfile updateProfile(Long id, SavedProfile updatedData) {
        return savedProfileRepository.findById(id).map(existingProfile -> {
            existingProfile.setName(updatedData.getName());
            existingProfile.setGender(updatedData.getGender());
            existingProfile.setYear(updatedData.getYear());
            existingProfile.setMonth(updatedData.getMonth());
            existingProfile.setDay(updatedData.getDay());
            existingProfile.setHour(updatedData.getHour());
            existingProfile.setMinute(updatedData.getMinute());
            existingProfile.setLocation(updatedData.getLocation());
            return savedProfileRepository.save(existingProfile);
        }).orElseThrow(() -> new RuntimeException("Profile not found with ID: " + id));
    }

    public void deleteProfile(Long id) {
        if (!savedProfileRepository.existsById(id)) {
            throw new RuntimeException("Profile not found with ID: " + id);
        }
        savedProfileRepository.deleteById(id);
    }
}