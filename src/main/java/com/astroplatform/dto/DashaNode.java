package com.astroplatform.dto;

import java.util.List;

public record DashaNode(
        String planet,
        String startDate,
        String endDate,
        boolean active, // <-- Safest naming convention for JSON
        List<DashaNode> subPeriods
) {}