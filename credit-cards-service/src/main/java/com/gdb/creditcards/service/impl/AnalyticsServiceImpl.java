package com.gdb.creditcards.service.impl;

import com.gdb.creditcards.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes portfolio analytics with a handful of grouped SQL aggregations.
 * Everything is read-only; the result is shaped for direct consumption by the
 * recharts components on the admin Analytics Command Center.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JdbcTemplate jdbc;

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public Map<String, Object> portfolioOverview() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary());
        out.put("cards_by_category", distribution("SELECT category AS name, COUNT(*) AS value "
                + "FROM credit_cards GROUP BY category ORDER BY category"));
        out.put("cards_by_status", distribution("SELECT status AS name, COUNT(*) AS value "
                + "FROM credit_cards GROUP BY status ORDER BY status"));
        out.put("cards_by_vendor", distribution("SELECT vendor AS name, COUNT(*) AS value "
                + "FROM credit_cards GROUP BY vendor ORDER BY vendor"));
        out.put("spend_by_channel", distribution(
                "SELECT channel AS name, COALESCE(SUM(amount),0) AS value "
                        + "FROM credit_card_transactions WHERE type = 'PURCHASE' "
                        + "GROUP BY channel ORDER BY value DESC"));
        out.put("spend_by_category", distribution(
                "SELECT c.category AS name, COALESCE(SUM(t.amount),0) AS value "
                        + "FROM credit_card_transactions t JOIN credit_cards c ON t.card_id = c.id "
                        + "WHERE t.type = 'PURCHASE' GROUP BY c.category ORDER BY value DESC"));
        out.put("monthly_spend", monthlySpend());
        out.put("top_cards_by_utilization", topCardsByUtilization());
        return out;
    }

    // ---- KPI summary -------------------------------------------------------
    private Map<String, Object> summary() {
        return jdbc.queryForObject("""
                SELECT
                    COUNT(*)                                                   AS total_cards,
                    COUNT(*) FILTER (WHERE status = 'ACTIVE')                  AS active_cards,
                    COUNT(*) FILTER (WHERE status = 'BLOCKED')                 AS blocked_cards,
                    COUNT(*) FILTER (WHERE status = 'INACTIVE')                AS inactive_cards,
                    COALESCE(SUM(credit_limit), 0)                            AS total_credit_limit,
                    COALESCE(SUM(outstanding_amount), 0)                      AS total_outstanding,
                    COALESCE(SUM(available_credit), 0)                        AS total_available
                FROM credit_cards
                """, (rs, n) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            long total = rs.getLong("total_cards");
            BigDecimal limit = rs.getBigDecimal("total_credit_limit");
            BigDecimal outstanding = rs.getBigDecimal("total_outstanding");
            m.put("total_cards", total);
            m.put("active_cards", rs.getLong("active_cards"));
            m.put("blocked_cards", rs.getLong("blocked_cards"));
            m.put("inactive_cards", rs.getLong("inactive_cards"));
            m.put("total_credit_limit", limit);
            m.put("total_outstanding", outstanding);
            m.put("total_available", rs.getBigDecimal("total_available"));
            m.put("overall_utilization", pct(outstanding, limit));
            // Card fee revenue is modelled as a small slice of outstanding for demo purposes.
            m.put("fee_revenue", outstanding.multiply(new BigDecimal("0.025")).setScale(2, RoundingMode.HALF_UP));
            return m;
        });
    }

    // ---- generic {name, value} distributions ------------------------------
    private List<Map<String, Object>> distribution(String sql) {
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbc.query(sql, rs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", rs.getString("name"));
            BigDecimal value = rs.getBigDecimal("value");
            m.put("value", value != null ? value : BigDecimal.ZERO);
            rows.add(m);
        });
        return rows;
    }

    // ---- last 6 months of purchases vs payments ---------------------------
    private List<Map<String, Object>> monthlySpend() {
        // Pre-seed 6 month buckets so the chart always has a continuous axis.
        Map<String, Map<String, Object>> buckets = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().withDayOfMonth(1).minusMonths(5);
        for (int i = 0; i < 6; i++) {
            LocalDate month = start.plusMonths(i);
            Map<String, Object> bucket = new LinkedHashMap<>();
            bucket.put("month", month.format(MONTH_KEY));
            bucket.put("purchases", BigDecimal.ZERO);
            bucket.put("payments", BigDecimal.ZERO);
            buckets.put(month.format(MONTH_KEY), bucket);
        }

        jdbc.query("""
                SELECT to_char(created_at, 'YYYY-MM') AS ym,
                       type,
                       COALESCE(SUM(amount), 0)       AS total
                FROM credit_card_transactions
                WHERE created_at >= date_trunc('month', CURRENT_DATE) - INTERVAL '5 months'
                GROUP BY ym, type
                """, rs -> {
            String ym = rs.getString("ym");
            Map<String, Object> bucket = buckets.get(ym);
            if (bucket == null) return;
            BigDecimal total = rs.getBigDecimal("total");
            if ("PURCHASE".equals(rs.getString("type"))) {
                bucket.put("purchases", total);
            } else if ("PAYMENT".equals(rs.getString("type"))) {
                bucket.put("payments", total);
            }
        });
        return new ArrayList<>(buckets.values());
    }

    // ---- top 5 cards by utilisation ---------------------------------------
    private List<Map<String, Object>> topCardsByUtilization() {
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbc.query("""
                SELECT id, card_holder_name, card_last4, category, credit_limit, outstanding_amount
                FROM credit_cards
                WHERE credit_limit > 0
                ORDER BY (outstanding_amount / NULLIF(credit_limit, 0)) DESC
                LIMIT 100
                """, rs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            BigDecimal limit = rs.getBigDecimal("credit_limit");
            BigDecimal outstanding = rs.getBigDecimal("outstanding_amount");
            m.put("id", rs.getString("id"));
            m.put("holder", rs.getString("card_holder_name"));
            m.put("last4", rs.getString("card_last4"));
            m.put("category", rs.getString("category"));
            m.put("credit_limit", limit);
            m.put("outstanding", outstanding);
            m.put("utilization", pct(outstanding, limit));
            rows.add(m);
        });
        return rows;
    }

    private static BigDecimal pct(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.signum() == 0) return BigDecimal.ZERO;
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 1, RoundingMode.HALF_UP);
    }
}
