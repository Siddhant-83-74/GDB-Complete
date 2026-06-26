package com.gdb.creditcards.util;

import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.exception.CreditCardException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads externalized named SQL queries from db/query/creditcard_queries.sql.
 * Queries are delimited by "-- QUERY_NAME" headers (same convention as the
 * other GDB services).
 */
public final class SqlLoader {

    private static final Map<String, String> QUERIES = new HashMap<>();
    private static final Pattern QUERY_PATTERN = Pattern.compile("--\\s*([A-Z_]+)\\s*\\n(.*?)(?=\\n--|$)",
            Pattern.DOTALL);

    static {
        loadQueries("db/query/creditcard_queries.sql");
    }

    private SqlLoader() {
    }

    private static void loadQueries(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            Matcher matcher = QUERY_PATTERN.matcher(content);
            while (matcher.find()) {
                QUERIES.put(matcher.group(1), matcher.group(2).trim());
            }
        } catch (IOException e) {
            throw new CreditCardException("Failed to load SQL queries from " + path,
                    CreditCardConstants.DATABASE_ERROR);
        }
    }

    public static String get(String queryName) {
        String query = QUERIES.get(queryName);
        if (query == null) {
            throw new CreditCardException("SQL query not found: " + queryName,
                    CreditCardConstants.DATABASE_ERROR);
        }
        return query;
    }
}
