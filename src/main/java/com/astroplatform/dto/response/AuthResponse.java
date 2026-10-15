package com.astroplatform.dto.response;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        Long userId ) {
}