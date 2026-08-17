package com.fabflix.web;

import com.fabflix.repository.MovieRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness/readiness check. Confirms both that the app booted and
 * that it can reach the database, now via the JPA repository layer instead
 * of a raw JdbcTemplate query.
 */
@RestController
public class HealthController {

    private final MovieRepository movieRepository;

    public HealthController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("app", "up");

        try {
            long movieCount = movieRepository.count();
            status.put("database", "up");
            status.put("movieCount", movieCount);
        } catch (Exception e) {
            status.put("database", "down");
            status.put("error", e.getMessage());
        }

        return status;
    }
}
