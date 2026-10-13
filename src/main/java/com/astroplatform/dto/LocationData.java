package com.astroplatform.dto;

public record LocationData(
        String locationName,
        double latitude,
        double longitude,
        String timezone // <-- ADD THIS 4TH ARGUMENT
) {}