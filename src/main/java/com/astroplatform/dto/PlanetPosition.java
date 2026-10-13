package com.astroplatform.dto;

public record  PlanetPosition(
        String name,
        String signName,
        int signNumber, // 1 for Aries, 2 for Taurus, etc.
        int degrees,
        int minutes,
        double absoluteDegree,
        boolean isRetrograde,
        int houseNumber,
        String nakshatraName,
        int nakshatraNumber,
        int nakshatraPada,
        double declination, // NEW: Required for exact Ayana Bala
        double speed

) {
}