package com.astroplatform.service;

import com.astroplatform.entity.PasswordResetToken;
import com.astroplatform.entity.User;
import com.astroplatform.repository.PasswordResetTokenRepository;
import com.astroplatform.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    @Value("${frontend.url}")
    private String feUrl;

    @Transactional
    public void processForgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // FIX: Instead of deleting, fetch the existing token or create a new one if it doesn't exist
            PasswordResetToken resetToken = tokenRepository.findByUser_Id(user.getId())
                    .orElse(new PasswordResetToken());

            // Update the token details
            String token = UUID.randomUUID().toString();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

            // Hibernate will now safely do an SQL UPDATE if it existed, or an INSERT if it's new
            tokenRepository.save(resetToken);

            sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
        }
    }

    @Transactional
    public void processResetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token used successfully, delete it
        tokenRepository.delete(resetToken);
    }

    private void sendPasswordResetEmail(String toEmail, String name, String token) {
        try {
            String resetUrl = feUrl + "/?token" + token;
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("Support AstroPlatform <platformastro@gmail.com>");
            helper.setSubject("Reset Your AstroPlatform Password");

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #4338ca;">AstroPlatform Password Reset</h2>
                        <p>Hello %s,</p>
                        <p>Click the button below to reset your password. This link expires in 1 hour.</p>
                        <a href="%s" style="background-color: #4f46e5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 20px 0;">Reset Password</a>
                        <p style="color: #6b7280; font-size: 12px;">If you didn't request this, safely ignore this email.</p>
                    </div>
                    """.formatted(name, resetUrl);

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}