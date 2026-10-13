package com.astroplatform.dto;

import java.util.List;

public record SingleChartData(
        double ascendantDegree,
        int ascendantSignNumber,
        String ascendantSignName,
        String ascendantFormattedDegree, // e.g. "9° 57'"
        String ascendantNakshatra,       // e.g. "Magha"
        int ascendantNakshatraPada,      // e.g. 3
        List<String> formattedHouseCusps,
        List<PlanetPosition> planets
) {}