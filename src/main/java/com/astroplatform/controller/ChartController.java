package com.astroplatform.controller;

import com.astroplatform.dto.ChartDetailsRequest;
import com.astroplatform.dto.CompleteChartResponse;
import com.astroplatform.service.ChartService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "${frontend.url}")
@RestController
@AllArgsConstructor
@RequestMapping("/api/chart")
public class ChartController {

    private final ChartService chartService;


    @PostMapping("/d1")
    public ResponseEntity<CompleteChartResponse> getChart(@RequestBody ChartDetailsRequest request) {
        CompleteChartResponse response = chartService.generateBirthChart(request);
        return ResponseEntity.ok(response);
    }
}