package com.fabflix.dto;

import java.util.List;

/** Shape returned by both /api/movies (as a list) and /api/single-movie (as one object). */
public record MovieSummaryDto(
        String id,
        String title,
        Integer year,
        String director,
        Double rating,
        String overview,
        String posterUrl,
        String backdropUrl,
        List<String> genres,
        List<StarRefDto> stars) {
}
