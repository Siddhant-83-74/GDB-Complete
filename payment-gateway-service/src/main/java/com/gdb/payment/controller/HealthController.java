package com.gdb.payment.controller;

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
    @Operation(summary = "Health check", description = "Returns current health status of the Payment Gateway Service")
    @ApiResponse(responseCode = "200", description = "Service is active")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "active",
                "service", "GDB-Payment-Gateway-Service"));
    }
}
