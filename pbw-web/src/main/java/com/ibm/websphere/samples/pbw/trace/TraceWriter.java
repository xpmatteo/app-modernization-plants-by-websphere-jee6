// ABOUTME: Writes trace events to JSONL files with sensitive data redaction.
// ABOUTME: Thread-safe file writing with support for scenario start/stop.

package com.ibm.websphere.samples.pbw.trace;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes trace events to JSONL files. Thread-safe for concurrent access.
 */
public class TraceWriter {

    private static final Logger LOGGER = Logger.getLogger(TraceWriter.class.getName());

    private static final String TRACE_DIR_PROPERTY = "phoenix.trace.dir";
    private static final String DEFAULT_TRACE_DIR = "./phoenix/traces";
    private static final String REDACTED = "[REDACTED]";

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "passwd", "pwd",
            "creditcard", "ccnum", "cardnumber",
            "cvv", "ccexpiremonth", "ccexpireyear",
            "cardholder"
    ));

    private static final ReentrantLock WRITE_LOCK = new ReentrantLock();
    private static BufferedWriter currentWriter;
    private static Path currentFile;

    /**
     * Get the trace output directory from configuration.
     */
    public static Path getTraceDir() {
        String dir = System.getProperty(TRACE_DIR_PROPERTY);
        if (dir == null || dir.isEmpty()) {
            dir = System.getenv("PHOENIX_TRACE_DIR");
        }
        if (dir == null || dir.isEmpty()) {
            dir = DEFAULT_TRACE_DIR;
        }
        return Paths.get(dir);
    }

    /**
     * Start a new scenario. Creates the JSONL file and writes the meta header.
     */
    public static void startScenario(String name, String description) {
        WRITE_LOCK.lock();
        try {
            if (currentWriter != null) {
                throw new IllegalStateException("Scenario already active");
            }

            Path traceDir = getTraceDir();
            Files.createDirectories(traceDir);

            currentFile = traceDir.resolve(name + ".jsonl");
            currentWriter = Files.newBufferedWriter(currentFile,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Write meta header
            StringBuilder meta = new StringBuilder();
            meta.append("{\"_meta\": {\"scenario\": ");
            appendJsonString(meta, name);
            meta.append(", \"description\": ");
            appendJsonString(meta, description != null ? description : "");
            meta.append("}}");

            currentWriter.write(meta.toString());
            currentWriter.newLine();
            currentWriter.flush();

            LOGGER.info("Started scenario: " + name + " -> " + currentFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start scenario", e);
            throw new RuntimeException("Failed to start scenario", e);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    /**
     * Stop the current scenario.
     */
    public static void stopScenario() {
        WRITE_LOCK.lock();
        try {
            if (currentWriter != null) {
                currentWriter.close();
                LOGGER.info("Stopped scenario, wrote to: " + currentFile);
            }
            currentWriter = null;
            currentFile = null;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing trace file", e);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    /**
     * Write an HTTP request event.
     */
    public static void writeHttpRequest(String method, String url, Map<String, String[]> params,
                                        Map<String, Object> session) {
        if (!ScenarioState.isActive()) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"traceId\": ");
        appendJsonString(json, TraceContext.getTraceId());
        json.append(", \"seq\": ").append(TraceContext.nextSequence());
        json.append(", \"layer\": \"http\", \"type\": \"request\"");
        json.append(", \"method\": ");
        appendJsonString(json, method);
        json.append(", \"url\": ");
        appendJsonString(json, url);

        if (params != null && !params.isEmpty()) {
            json.append(", \"params\": ");
            appendParamsJson(json, params);
        }

        json.append(", \"session\": ");
        appendMapJson(json, session);
        json.append("}");

        writeLine(json.toString());
    }

    /**
     * Write an HTTP response event.
     */
    public static void writeHttpResponse(int status, String redirect, Map<String, Object> session) {
        if (!ScenarioState.isActive()) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"traceId\": ");
        appendJsonString(json, TraceContext.getTraceId());
        json.append(", \"seq\": ").append(TraceContext.nextSequence());
        json.append(", \"layer\": \"http\", \"type\": \"response\"");
        json.append(", \"status\": ").append(status);

        if (redirect != null) {
            json.append(", \"redirect\": ");
            appendJsonString(json, redirect);
        }

        json.append(", \"session\": ");
        appendMapJson(json, session);
        json.append("}");

        writeLine(json.toString());
    }

    /**
     * Write an entrypoint (method invocation) event.
     */
    public static void writeEntrypoint(String className, String methodName, String action) {
        if (!ScenarioState.isActive()) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"traceId\": ");
        appendJsonString(json, TraceContext.getTraceId());
        json.append(", \"seq\": ").append(TraceContext.nextSequence());
        json.append(", \"layer\": \"entrypoint\"");
        json.append(", \"class\": ");
        appendJsonString(json, className);
        json.append(", \"method\": ");
        appendJsonString(json, methodName);

        if (action != null) {
            json.append(", \"action\": ");
            appendJsonString(json, action);
        }

        json.append("}");

        writeLine(json.toString());
    }

    /**
     * Write a JDBC query event with results.
     */
    public static void writeJdbcQuery(String sql, List<Object> params, List<Map<String, Object>> results) {
        if (!ScenarioState.isActive()) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"traceId\": ");
        appendJsonString(json, TraceContext.getTraceId());
        json.append(", \"seq\": ").append(TraceContext.nextSequence());
        json.append(", \"layer\": \"jdbc\", \"type\": \"query\"");
        json.append(", \"sql\": ");
        appendJsonString(json, sql);

        json.append(", \"params\": ");
        appendListJson(json, params);

        if (results != null) {
            json.append(", \"results\": ");
            appendResultsJson(json, results);
        }

        json.append("}");

        writeLine(json.toString());
    }

    /**
     * Write a JDBC update event (INSERT/UPDATE/DELETE).
     */
    public static void writeJdbcUpdate(String sql, List<Object> params, int affectedRows) {
        if (!ScenarioState.isActive()) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"traceId\": ");
        appendJsonString(json, TraceContext.getTraceId());
        json.append(", \"seq\": ").append(TraceContext.nextSequence());
        json.append(", \"layer\": \"jdbc\", \"type\": \"update\"");
        json.append(", \"sql\": ");
        appendJsonString(json, sql);

        json.append(", \"params\": ");
        appendListJson(json, params);

        json.append(", \"affectedRows\": ").append(affectedRows);
        json.append("}");

        writeLine(json.toString());
    }

    private static void writeLine(String line) {
        WRITE_LOCK.lock();
        try {
            if (currentWriter != null) {
                currentWriter.write(line);
                currentWriter.newLine();
                currentWriter.flush();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error writing trace line", e);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    private static void appendJsonString(StringBuilder sb, String value) {
        if (value == null) {
            sb.append("null");
            return;
        }
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void appendParamsJson(StringBuilder sb, Map<String, String[]> params) {
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if (!first) sb.append(", ");
            first = false;

            appendJsonString(sb, entry.getKey());
            sb.append(": ");

            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                sb.append("null");
            } else if (values.length == 1) {
                appendJsonValue(sb, entry.getKey(), values[0]);
            } else {
                sb.append("[");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) sb.append(", ");
                    appendJsonValue(sb, entry.getKey(), values[i]);
                }
                sb.append("]");
            }
        }
        sb.append("}");
    }

    private static void appendMapJson(StringBuilder sb, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            sb.append("{}");
            return;
        }
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            first = false;

            appendJsonString(sb, entry.getKey());
            sb.append(": ");
            appendJsonValue(sb, entry.getKey(), entry.getValue());
        }
        sb.append("}");
    }

    private static void appendListJson(StringBuilder sb, List<Object> list) {
        if (list == null || list.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            appendJsonValue(sb, null, list.get(i));
        }
        sb.append("]");
    }

    private static void appendResultsJson(StringBuilder sb, List<Map<String, Object>> results) {
        sb.append("[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) sb.append(", ");
            appendMapJson(sb, results.get(i));
        }
        sb.append("]");
    }

    private static void appendJsonValue(StringBuilder sb, String fieldName, Object value) {
        // Check for sensitive field redaction
        if (fieldName != null && isSensitiveField(fieldName)) {
            appendJsonString(sb, REDACTED);
            return;
        }

        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            appendJsonString(sb, (String) value);
        } else if (value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof byte[]) {
            appendBinaryJson(sb, (byte[]) value);
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            appendMapJson(sb, map);
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            appendListJson(sb, list);
        } else {
            appendJsonString(sb, value.toString());
        }
    }

    private static void appendBinaryJson(StringBuilder sb, byte[] data) {
        sb.append("{\"_binary\": true, \"hash\": \"sha256:");
        sb.append(sha256Hex(data));
        sb.append("\", \"size\": ");
        sb.append(data.length);
        sb.append("}");
    }

    private static boolean isSensitiveField(String fieldName) {
        String lowerName = fieldName.toLowerCase();
        // Check for exact match first
        if (SENSITIVE_FIELDS.contains(lowerName)) {
            return true;
        }
        // Check if any sensitive field name is contained in the field name
        // This handles cases like "loginForm:passwd" or "user_password"
        for (String sensitive : SENSITIVE_FIELDS) {
            if (lowerName.contains(sensitive)) {
                return true;
            }
        }
        return false;
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "error";
        }
    }
}
