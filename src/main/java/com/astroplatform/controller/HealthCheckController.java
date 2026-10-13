package com.astroplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("health")
@RestController
public class HealthCheckController {

    @GetMapping("ping")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().body("pong");
    }

}