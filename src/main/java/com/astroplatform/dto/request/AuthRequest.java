package com.astroplatform.dto.request;

public record AuthRequest(
        String email,
        String password,
        String fullName,
        String phoneNumber,
        Integer age ) {
}