package com.gdb.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Service health monitoring")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns current health status of the Company CRV Service")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = Map.of(
                "service", "company_crv_service",
                "status", "healthy",
                "version", "1.0.0");
        return ResponseEntity.ok(health);
    }
}
