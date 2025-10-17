// ABOUTME: Health check controller for Spring Boot Plants by WebSphere application
// ABOUTME: Provides /ready endpoint to check database connectivity and application readiness
package it.xpug.pbw.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test database connectivity with a simple query
            Integer result = jdbcTemplate.queryForObject("SELECT 2", Integer.class);

            if (result != null && result == 2) {
                response.put("status", "ready");
                response.put("database", "connected");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "not ready");
                response.put("database", "unexpected result");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

        } catch (Exception e) {
            response.put("status", "not ready");
            response.put("database", "disconnected");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
