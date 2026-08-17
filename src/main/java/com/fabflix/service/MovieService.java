package com.fabflix.service;

import com.fabflix.dto.MovieBriefDto;
import com.fabflix.dto.MovieSearchParams;
import com.fabflix.dto.MovieSummaryDto;
import com.fabflix.dto.StarRefDto;
import com.fabflix.entity.Genre;
import com.fabflix.entity.Movie;
import com.fabflix.repository.MovieRepository;
import com.fabflix.repository.MovieSpecifications;
import com.fabflix.util.TmdbImageUrls;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieSummaryDto> search(MovieSearchParams params) {
        List<Specification<Movie>> specs = new ArrayList<>();

        String fulltext = trim(params.fulltext());
        if (!fulltext.isEmpty()) {
            // Fulltext mode: title tokens only, every other filter is ignored —
            // matches the original MovieListServlet's fulltext branch.
            specs.add(MovieSpecifications.titleContainsAllTokens(fulltext));
        } else {
            String title = trim(params.title());
            if (!title.isEmpty()) {
                specs.add(MovieSpecifications.titleContains(title));
            }
            if (params.year() != null) {
                specs.add(MovieSpecifications.yearEquals(params.year()));
            }
            String director = trim(params.director());
            if (!director.isEmpty()) {
                specs.add(MovieSpecifications.directorContains(director));
            }
            String startsWith = trim(params.startsWith());
            if (!startsWith.isEmpty()) {
                specs.add("0".equals(startsWith)
                        ? MovieSpecifications.titleStartsWithDigit()
                        : MovieSpecifications.titleStartsWithLetter(startsWith));
            }
            String genre = trim(params.genre());
            if (!genre.isEmpty()) {
                specs.add(MovieSpecifications.genreNameEquals(genre));
            }
            String star = trim(params.star());
            if (!star.isEmpty()) {
                specs.add(MovieSpecifications.starNameContains(star));
            }
        }

        String orderBy = trim(params.orderBy());
        // Note: matches the original servlet's quirk of defaulting to descending
        // order whenever "dir" is absent or not exactly "asc".
        boolean asc = "asc".equalsIgnoreCase(trim(params.dir()));
        specs.add("rating_title".equals(orderBy)
                ? MovieSpecifications.sortByRatingThenTitle(asc)
                : MovieSpecifications.sortByTitleThenRating(asc));

        Specification<Movie> combined = Specification.allOf(specs);

        int n = params.n() > 0 ? params.n() : 10;
        int page = params.page() > 0 ? params.page() : 1;
        // Sort.unsorted() is required here: the sort spec above already calls
        // query.orderBy() itself, and Spring Data only overwrites that if the
        // Pageable carries its own Sort.
        Pageable pageable = PageRequest.of(page - 1, n, Sort.unsorted());

        return movieRepository.findAll(combined, pageable)
                .getContent()
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public Optional<MovieSummaryDto> findById(String id) {
        return movieRepository.findById(id).map(this::toSummaryDto);
    }

    public List<MovieBriefDto> findTitleMatches(String query, int limit) {
        Specification<Movie> spec = MovieSpecifications.titleContainsAllTokens(query);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("title").ascending());
        return movieRepository.findAll(spec, pageable)
                .getContent()
                .stream()
                .map(m -> new MovieBriefDto(
                        m.getId(), m.getTitle(), m.getYear(), m.getDirector(),
                        TmdbImageUrls.poster(m.getPosterPath())))
                .toList();
    }

    private MovieSummaryDto toSummaryDto(Movie m) {
        Double rating = m.getRatingInfo() != null && m.getRatingInfo().getRating() != null
                ? m.getRatingInfo().getRating().doubleValue()
                : 0.0;
        List<String> genres = m.getGenres().stream().map(Genre::getName).toList();
        List<StarRefDto> stars = m.getStars().stream()
                .map(s -> new StarRefDto(s.getId(), s.getName(), TmdbImageUrls.profile(s.getProfilePath())))
                .toList();
        return new MovieSummaryDto(
                m.getId(),
                m.getTitle(),
                m.getYear(),
                m.getDirector(),
                rating,
                m.getOverview(),
                TmdbImageUrls.poster(m.getPosterPath()),
                TmdbImageUrls.backdrop(m.getBackdropPath()),
                genres,
                stars);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
