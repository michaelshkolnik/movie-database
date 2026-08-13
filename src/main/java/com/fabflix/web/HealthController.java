package com.fabflix.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple liveness/readiness check used while the rest of the API is being ported over.
 * Confirms both that the app booted and that it can reach the database.
 */
@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("app", "up");

        try {
            Integer movieCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM movies", Integer.class);
            status.put("database", "up");
            status.put("movieCount", movieCount);
        } catch (Exception e) {
            status.put("database", "down");
            status.put("error", e.getMessage());
        }

        return status;
    }
}
