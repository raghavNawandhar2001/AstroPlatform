package com.astroplatform.dto;

public record PanchangRequest(
        int year,
        int month,
        int day,
        String location
) {}