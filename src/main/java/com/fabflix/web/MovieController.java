package com.fabflix.web;

import com.fabflix.dto.AutocompleteSuggestionDto;
import com.fabflix.dto.ErrorDto;
import com.fabflix.dto.MovieBriefDto;
import com.fabflix.dto.MovieSearchParams;
import com.fabflix.service.MovieService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Replaces MovieListServlet, SingleMovieServlet, AutocompleteServlet and
 * FullTextSearchServlet, preserving their JSON contracts for the existing
 * static frontend.
 */
@RestController
@RequestMapping("/api")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies")
    public List<?> movies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String fulltext,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String star,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String startsWith,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String dir,
            @RequestParam(required = false) String n,
            @RequestParam(required = false) String page) {

        MovieSearchParams params = new MovieSearchParams(
                title,
                fulltext,
                parseIntOrNull(year),
                director,
                star,
                genre,
                startsWith,
                orderBy,
                dir,
                parsePositiveInt(n, 10),
                parsePositiveInt(page, 1));

        return movieService.search(params);
    }

    @GetMapping("/single-movie")
    public Object singleMovie(@RequestParam(required = false) String id) {
        if (id == null || id.isBlank()) {
            return new ErrorDto("No ID provided");
        }
        return movieService.findById(id).<Object>map(m -> m).orElseGet(() -> new ErrorDto("No movie found"));
    }

    @GetMapping("/autocomplete")
    public List<AutocompleteSuggestionDto> autocomplete(@RequestParam(required = false) String query) {
        String decoded = query == null ? "" : query.trim();
        if (decoded.length() < 3) {
            return List.of();
        }
        return movieService.findTitleMatches(decoded, 10).stream()
                .map(m -> new AutocompleteSuggestionDto(m.title(), m.id()))
                .toList();
    }

    @GetMapping("/movies-fulltext")
    public List<MovieBriefDto> moviesFulltext(@RequestParam(required = false) String query) {
        String decoded = query == null ? "" : query.trim();
        if (decoded.isEmpty()) {
            return List.of();
        }
        return movieService.findTitleMatches(decoded, 50);
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parsePositiveInt(String s, int defaultValue) {
        if (s == null || s.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(s.trim()));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
