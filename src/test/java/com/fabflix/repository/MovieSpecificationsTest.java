package com.fabflix.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fabflix.entity.Genre;
import com.fabflix.entity.Movie;
import com.fabflix.entity.Rating;
import com.fabflix.entity.Star;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Regression test for a bug that actually shipped: MySQL rejects
 * "SELECT DISTINCT ... ORDER BY <expr not in the SELECT list>", which
 * genreNameEquals/starNameContains + the rating-based sort specs used to
 * trigger via query.distinct(true) (see git history: "Fix 500 on
 * genre/star/fulltext search"). Runs against a real MySQL container rather
 * than H2 because that incompatibility is specific to MySQL's dialect --
 * an embedded database would happily accept the old, broken query and this
 * test would pass even if the bug came back.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Testcontainers
class MovieSpecificationsTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private StarRepository starRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    void seed() {
        Genre action = new Genre();
        action.setId(28);
        action.setName("Action");
        genreRepository.save(action);

        Genre drama = new Genre();
        drama.setId(18);
        drama.setName("Drama");
        genreRepository.save(drama);

        Star brando = new Star();
        brando.setId("1");
        brando.setName("Marlon Brando");
        starRepository.save(brando);

        Star brandon = new Star();
        brandon.setId("2");
        brandon.setName("Brandon Lee");
        starRepository.save(brandon);

        Movie godfather = new Movie();
        godfather.setId("100");
        godfather.setTitle("The Godfather");
        godfather.setYear(1972);
        godfather.setDirector("Francis Ford Coppola");
        godfather.setGenres(Set.of(action, drama));
        // Both stars match a "Brando" substring search on the SAME movie --
        // exactly the fan-out that used to require query.distinct(true).
        godfather.setStars(Set.of(brando, brandon));
        movieRepository.save(godfather);

        Rating rating = new Rating();
        rating.setMovie(godfather);
        rating.setRating(9.2f);
        rating.setVoteCount(1000);
        ratingRepository.save(rating);
    }

    @Test
    void genreSearchCombinedWithRatingSortDoesNotThrow() {
        Specification<Movie> spec = Specification.allOf(
                MovieSpecifications.genreNameEquals("Action"),
                MovieSpecifications.sortByRatingThenTitle(false));
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        List<Movie> results = movieRepository.findAll(spec, pageable).getContent();

        assertThat(results).extracting(Movie::getId).containsExactly("100");
    }

    @Test
    void starSearchCombinedWithRatingSortDoesNotDuplicateFannedOutMovie() {
        // "Brando" substring-matches BOTH stars on the same movie -- this
        // guards the fix's correctness (correlated subquery, no fan-out),
        // not just that the query avoids throwing.
        Specification<Movie> spec = Specification.allOf(
                MovieSpecifications.starNameContains("Brando"),
                MovieSpecifications.sortByTitleThenRating(true));
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        List<Movie> results = movieRepository.findAll(spec, pageable).getContent();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("100");
    }

    @Test
    void fulltextSearchRequiresEveryTokenToMatch() {
        Specification<Movie> matching = Specification.allOf(
                MovieSpecifications.titleContainsAllTokens("godfather"),
                MovieSpecifications.sortByRatingThenTitle(true));
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        assertThat(movieRepository.findAll(matching, pageable).getContent()).hasSize(1);

        Specification<Movie> notMatching = Specification.allOf(
                MovieSpecifications.titleContainsAllTokens("godfather part ii"),
                MovieSpecifications.sortByRatingThenTitle(true));

        assertThat(movieRepository.findAll(notMatching, pageable).getContent()).isEmpty();
    }
}
