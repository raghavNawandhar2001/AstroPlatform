package com.astroplatform.repository;

import com.astroplatform.entity.SavedProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedProfileRepository extends JpaRepository<SavedProfile, Long> {

    // Fetch all profiles belonging to a specific user
    List<SavedProfile> findByUserId(Long userId);

    // Verify a specific profile belongs to a specific user before editing/deleting
    Optional<SavedProfile> findByIdAndUserId(Long id, Long userId);
}