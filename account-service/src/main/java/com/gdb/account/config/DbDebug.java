package com.gdb.account.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DbDebug implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbDebug(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("DEBUG: Querying pg_type for gender types...");
            List<Map<String, Object>> types = jdbcTemplate.queryForList(
                    "SELECT n.nspname, t.typname FROM pg_type t JOIN pg_namespace n ON n.oid = t.typnamespace WHERE t.typname ILIKE '%gender%'");
            log.info("DEBUG TYPES FOUND: {}", types);

            log.info("DEBUG: Inspecting savings_account_details.gender column type...");
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, udt_name, udt_schema FROM information_schema.columns WHERE table_name = 'savings_account_details' AND column_name = 'gender'");
            log.info("DEBUG COLUMN INFO: {}", cols);

        } catch (Exception e) {
            log.error("DEBUG ERROR", e);
        }
    }
}
