package com.astroplatform.controller;

import com.astroplatform.config.jwt.JwtUtil;
import com.astroplatform.dto.request.AuthRequest;
import com.astroplatform.dto.response.AuthResponse;
import com.astroplatform.entity.User;
import com.astroplatform.repository.PasswordResetTokenRepository;
import com.astroplatform.repository.UserRepository;
import com.astroplatform.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${frontend.url}")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender, AuthService authService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body("Email is already registered.");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setAge(request.age());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        var userOptional = userRepository.findByEmail(request.email());
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.password(), userOptional.get().getPassword())) {
            return ResponseEntity.badRequest().body("Invalid email or password.");
        }
        User user = userOptional.get();
        return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(
                user.getEmail()),
                user.getEmail(),
                user.getFullName(),
                user.getId()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        authService.processForgotPassword(request.get("email"));

        // Always return 200 OK so hackers can't guess which emails exist in your DB
        return ResponseEntity.ok("If that email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            authService.processResetPassword(request.get("token"), request.get("newPassword"));
            return ResponseEntity.ok("Password reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}