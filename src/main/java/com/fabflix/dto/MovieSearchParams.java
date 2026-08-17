package com.fabflix.dto;

/**
 * Parsed/normalized query parameters for /api/movies. Built by the
 * controller from raw request params so MovieService doesn't deal with
 * Strings-that-might-be-null-or-blank directly.
 */
public record MovieSearchParams(
        String title,
        String fulltext,
        Integer year,
        String director,
        String star,
        String genre,
        String startsWith,
        String orderBy,
        String dir,
        int n,
        int page) {
}
