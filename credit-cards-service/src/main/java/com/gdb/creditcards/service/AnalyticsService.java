package com.gdb.creditcards.service;

import java.util.Map;

/**
 * Read-only portfolio aggregations powering the admin Analytics Command Center.
 * Returns a chart-friendly, denormalised view across all credit cards.
 */
public interface AnalyticsService {

    /** Aggregate KPIs and distributions across the entire card portfolio. */
    Map<String, Object> portfolioOverview();
}
