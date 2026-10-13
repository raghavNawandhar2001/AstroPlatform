package com.astroplatform.repository;

import com.astroplatform.entity.SavedProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedProfileRepository extends JpaRepository<SavedProfile, Long> {
}