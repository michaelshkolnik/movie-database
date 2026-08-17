package com.fabflix.repository;

import com.fabflix.entity.Genre;
import com.fabflix.entity.Movie;
import com.fabflix.entity.Rating;
import com.fabflix.entity.Star;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic query pieces for {@code /api/movies}, mirroring the filter/sort
 * behavior of the original MongoDB-backed MovieListServlet.
 *
 * The two sortBy* specifications deliberately reach into the CriteriaQuery
 * and call query.orderBy(...) themselves (LEFT JOIN to ratings + coalesce to
 * 0 for movies without a ratings row), instead of relying on Spring Data's
 * automatic Sort-by-nested-property mechanism, which can't null-coalesce
 * across an optional association. Callers must pass an unsorted Pageable so
 * Spring Data doesn't overwrite this ordering afterward.
 */
public final class MovieSpecifications {

    private MovieSpecifications() {
    }

    public static Specification<Movie> titleContains(String title) {
        String needle = "%" + title.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), needle);
    }

    public static Specification<Movie> titleContainsAllTokens(String text) {
        String[] tokens = text.trim().split("\\s+");
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            for (String token : tokens) {
                if (!token.isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + token.toLowerCase() + "%"));
                }
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Movie> yearEquals(int year) {
        return (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    public static Specification<Movie> directorContains(String director) {
        String needle = "%" + director.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("director")), needle);
    }

    public static Specification<Movie> starNameContains(String star) {
        String needle = "%" + star.toLowerCase() + "%";
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Movie, Star> stars = root.join("stars");
            return cb.like(cb.lower(stars.get("name")), needle);
        };
    }

    public static Specification<Movie> genreNameEquals(String genre) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Movie, Genre> genres = root.join("genres");
            return cb.equal(genres.get("name"), genre);
        };
    }

    public static Specification<Movie> titleStartsWithLetter(String letter) {
        String needle = letter.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), needle);
    }

    public static Specification<Movie> titleStartsWithDigit() {
        return (root, query, cb) -> {
            Expression<String> firstChar = cb.substring(root.get("title"), 1, 1);
            return cb.between(firstChar, "0", "9");
        };
    }

    public static Specification<Movie> sortByTitleThenRating(boolean asc) {
        return (root, query, cb) -> {
            Expression<Number> ratingExpr = ratingExpression(root, cb);
            query.orderBy(asc
                    ? List.of(cb.asc(root.get("title")), cb.asc(ratingExpr))
                    : List.of(cb.desc(root.get("title")), cb.desc(ratingExpr)));
            return cb.conjunction();
        };
    }

    public static Specification<Movie> sortByRatingThenTitle(boolean asc) {
        return (root, query, cb) -> {
            Expression<Number> ratingExpr = ratingExpression(root, cb);
            query.orderBy(asc
                    ? List.of(cb.asc(ratingExpr), cb.asc(root.get("title")))
                    : List.of(cb.desc(ratingExpr), cb.desc(root.get("title"))));
            return cb.conjunction();
        };
    }

    private static Expression<Number> ratingExpression(
            jakarta.persistence.criteria.Root<Movie> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
        Join<Movie, Rating> ratingInfo = root.join("ratingInfo", JoinType.LEFT);
        return cb.coalesce(ratingInfo.get("rating"), 0.0);
    }
}
