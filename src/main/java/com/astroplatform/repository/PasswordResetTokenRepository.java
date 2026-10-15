package com.astroplatform.repository;

import com.astroplatform.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser_Id(Long userId);

    Optional<PasswordResetToken> findByUser_Id(Long userId);
}