package com.fabflix.util;

/**
 * Builds full TMDb image CDN URLs from the relative paths stored in the
 * database (e.g. "/abc123.jpg"). TMDb's own configuration endpoint lists
 * several sizes per image type; these are fixed, reasonable defaults for a
 * server-rendered catalog rather than a responsive image-set.
 */
public final class TmdbImageUrls {
    private TmdbImageUrls() {}

    private static final String BASE_URL = "https://image.tmdb.org/t/p/";

    private static final String POSTER_SIZE = "w500";
    private static final String BACKDROP_SIZE = "w1280";
    private static final String PROFILE_SIZE = "w185";

    public static String poster(String posterPath) {
        return build(posterPath, POSTER_SIZE);
    }

    public static String backdrop(String backdropPath) {
        return build(backdropPath, BACKDROP_SIZE);
    }

    public static String profile(String profilePath) {
        return build(profilePath, PROFILE_SIZE);
    }

    private static String build(String path, String size) {
        if (path == null || path.isBlank()) {
            return null;
        }
        return BASE_URL + size + path;
    }
}
