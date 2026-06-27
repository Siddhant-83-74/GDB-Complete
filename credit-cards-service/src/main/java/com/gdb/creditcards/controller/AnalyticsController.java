package com.gdb.creditcards.controller;

import com.gdb.creditcards.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin analytics endpoints for the Credit Card Command Center. Read-only
 * portfolio aggregations across every card in the system.
 */
@RestController
@RequestMapping("/api/v1/credit-cards/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/portfolio")
    public ResponseEntity<Map<String, Object>> portfolio() {
        return ResponseEntity.ok(analyticsService.portfolioOverview());
    }
}
