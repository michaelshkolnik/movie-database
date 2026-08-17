package com.fabflix.dto;

/** Shape returned by /api/movies-fulltext — no rating/genres/stars, matching the original servlet. */
public record MovieBriefDto(String id, String title, Integer year, String director, String posterUrl) {
}
