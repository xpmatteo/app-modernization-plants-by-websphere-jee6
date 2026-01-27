// ABOUTME: Custom EclipseLink SessionLog that captures SQL queries for tracing.
// ABOUTME: Wraps the default logger and writes SQL events to TraceWriter.

package com.ibm.websphere.samples.pbw.trace;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom EclipseLink SessionLog that captures SQL queries when tracing is active.
 * Delegates non-SQL logging to the original session log.
 */
public class TracingSessionLog extends AbstractSessionLog {

    private final SessionLog delegate;

    // Pattern to detect SQL statements
    private static final Pattern SQL_PATTERN = Pattern.compile(
            "^\\s*(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP)\\s+",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to extract bind parameters from EclipseLink log format
    // Format: "bind => [value1, value2, ...]"
    private static final Pattern BIND_PATTERN = Pattern.compile(
            "bind\\s*=>\\s*\\[(.*)\\]",
            Pattern.CASE_INSENSITIVE
    );

    // Stores the last SQL statement to pair with bind parameters
    private final ThreadLocal<String> lastSql = new ThreadLocal<>();

    public TracingSessionLog(SessionLog delegate) {
        this.delegate = delegate;
        // Match the delegate's log level
        if (delegate != null) {
            this.level = delegate.getLevel();
        }
    }

    @Override
    public void log(SessionLogEntry entry) {
        // Always delegate to original logger
        if (delegate != null) {
            delegate.log(entry);
        }

        // Only capture SQL when tracing is active
        if (!TraceContext.isActive()) {
            lastSql.remove();
            return;
        }

        String message = entry.getMessage();
        if (message == null) {
            return;
        }

        // Check if this is a SQL category message
        String category = entry.getNameSpace();
        if ("sql".equalsIgnoreCase(category)) {
            handleSqlLogEntry(message);
        }
    }

    private void handleSqlLogEntry(String message) {
        // Check for bind parameter line
        Matcher bindMatcher = BIND_PATTERN.matcher(message);
        if (bindMatcher.find()) {
            String bindValues = bindMatcher.group(1);
            List<Object> params = parseBindValues(bindValues);
            String sql = lastSql.get();

            if (sql != null) {
                writeTrace(sql, params);
                lastSql.remove();
            }
            return;
        }

        // Check for SQL statement
        if (SQL_PATTERN.matcher(message).find()) {
            // Clean up the SQL (remove extra whitespace)
            String sql = message.trim();
            lastSql.set(sql);

            // If no bind parameters follow, write trace without params
            // This handles statements without parameters
        }
    }

    private List<Object> parseBindValues(String bindValues) {
        List<Object> params = new ArrayList<>();
        if (bindValues == null || bindValues.trim().isEmpty()) {
            return params;
        }

        // Simple parsing - split by comma, handling quoted strings
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        int depth = 0;

        for (int i = 0; i < bindValues.length(); i++) {
            char c = bindValues.charAt(i);

            if (c == '\'' && (i == 0 || bindValues.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
                current.append(c);
            } else if (!inQuote && (c == '[' || c == '{')) {
                depth++;
                current.append(c);
            } else if (!inQuote && (c == ']' || c == '}')) {
                depth--;
                current.append(c);
            } else if (!inQuote && depth == 0 && c == ',') {
                params.add(parseValue(current.toString().trim()));
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // Add last parameter
        String lastParam = current.toString().trim();
        if (!lastParam.isEmpty()) {
            params.add(parseValue(lastParam));
        }

        return params;
    }

    private Object parseValue(String value) {
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }

        // Remove surrounding quotes
        if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
            return value.substring(1, value.length() - 1);
        }

        // Try to parse as number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Return as string
            return value;
        }
    }

    private void writeTrace(String sql, List<Object> params) {
        String upperSql = sql.toUpperCase().trim();

        if (upperSql.startsWith("SELECT")) {
            // For SELECT queries, we'll write with null results
            // since EclipseLink doesn't give us the results via logging
            TraceWriter.writeJdbcQuery(sql, params, null);
        } else {
            // For INSERT/UPDATE/DELETE, write as update
            TraceWriter.writeJdbcUpdate(sql, params, -1);
        }
    }

    // Delegate methods

    @Override
    public boolean shouldLog(int level, String category) {
        // Always log SQL at FINE level to capture queries
        if ("sql".equalsIgnoreCase(category) && TraceContext.isActive()) {
            return true;
        }
        return delegate != null ? delegate.shouldLog(level, category) : super.shouldLog(level, category);
    }

    @Override
    public int getLevel() {
        return delegate != null ? delegate.getLevel() : super.getLevel();
    }

    @Override
    public void setLevel(int level) {
        if (delegate != null) {
            delegate.setLevel(level);
        }
        super.setLevel(level);
    }

    @Override
    public int getLevel(String category) {
        // For SQL category, return FINE to ensure we capture queries
        if ("sql".equalsIgnoreCase(category) && TraceContext.isActive()) {
            return SessionLog.FINE;
        }
        return delegate != null ? delegate.getLevel(category) : super.getLevel(category);
    }

    @Override
    public void setLevel(int level, String category) {
        if (delegate != null) {
            delegate.setLevel(level, category);
        }
        super.setLevel(level, category);
    }
}
