package com.astroplatform.controller;

import com.astroplatform.dto.PanchangRequest;
import com.astroplatform.dto.PanchangResponse;
import com.astroplatform.service.PanchangService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/panchang")
@CrossOrigin(origins = "*") 
@AllArgsConstructor
public class PanchangController {

    private final PanchangService panchangService;

    @PostMapping("/daily")
    public ResponseEntity<PanchangResponse> getDailyPanchang(@RequestBody PanchangRequest request) {
        return ResponseEntity.ok(panchangService.getDailyPanchang(request));
    }
}