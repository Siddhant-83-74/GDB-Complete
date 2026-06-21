package com.gdb.account.util;

import com.gdb.account.exception.AccountException;
import com.gdb.account.constants.AccountConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to load externalized SQL queries from resource files.
 */
public class SqlLoader {

    private static final Map<String, String> QUERIES = new HashMap<>();
    private static final Pattern QUERY_PATTERN = Pattern.compile("--\\s*([A-Z_]+)\\s*\\n(.*?)(?=\\n--|$)",
            Pattern.DOTALL);

    static {
        loadQueries("db/query/account_queries.sql");
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
            throw new AccountException("Failed to load SQL queries from " + path, AccountConstants.DATABASE_ERROR);
        }
    }

    public static String get(String queryName) {
        String query = QUERIES.get(queryName);
        if (query == null) {
            throw new AccountException("SQL query not found: " + queryName, AccountConstants.DATABASE_ERROR);
        }
        return query;
    }

    private SqlLoader() {
    }
}
