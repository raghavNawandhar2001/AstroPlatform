package com.astroplatform.dto;

public record ShadbalaItem(
        String planet,
        double sthanaBala,     // Positional Strength
        double digBala,        // Directional Strength
        double kalaBala,       // Temporal Strength
        double chestaBala,     // Motivational/Retrogression Strength
        double naisargikaBala, // Natural Strength
        double drikBala,       // Aspectual Strength
        double totalRupas,     // Total Strength in Rupas (Sum of Virupas / 60)
        double requiredRupas   // BPHS Minimum Required Rupas
) {}