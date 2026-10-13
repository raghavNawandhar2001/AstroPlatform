package com.astroplatform.dto;

import java.util.List;
import java.util.Map;

public record CompleteChartResponse(
        String name,
        String gender,
        String location,
        String timezoneDetected,
        AstronomicalData astronomicalData,
        Map<String, SingleChartData> divisionalCharts,
        List<DashaNode> dashas,

        // --- NEW ADVANCED DATA ---
        JaiminiDetails jaimini,
        List<ShadbalaItem> shadbala,
        Map<String, int[]> ashtakvarga // 12-element array representing points for Aries through Pisces
) {}