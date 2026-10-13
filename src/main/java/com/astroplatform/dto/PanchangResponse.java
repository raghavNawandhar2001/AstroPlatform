package com.astroplatform.dto;

import java.util.List;

public record PanchangResponse(
        String date,
        String location,
        String sunriseTime,
        String sunsetTime,
        String vara,
        String tithi,
        String nakshatra,
        String yoga,
        String karana,
        String sunSign,
        String moonSign,
        List<PlanetPosition> planets // <-- NEW FIELD
) {}