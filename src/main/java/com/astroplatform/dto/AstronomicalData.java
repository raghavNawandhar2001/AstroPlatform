package com.astroplatform.dto;

public record AstronomicalData(
        double ayanamsaDegree,
        double siderealTimeHours,
        String sunriseTime,
        String sunsetTime,
        String nextSolarEclipseTime
) {}