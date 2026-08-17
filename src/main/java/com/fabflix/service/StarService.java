package com.fabflix.service;

import com.fabflix.dto.MovieRefDto;
import com.fabflix.dto.StarDetailDto;
import com.fabflix.entity.Movie;
import com.fabflix.entity.Star;
import com.fabflix.repository.MovieRepository;
import com.fabflix.repository.StarRepository;
import com.fabflix.util.TmdbImageUrls;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StarService {

    private final StarRepository starRepository;
    private final MovieRepository movieRepository;

    public StarService(StarRepository starRepository, MovieRepository movieRepository) {
        this.starRepository = starRepository;
        this.movieRepository = movieRepository;
    }

    public Optional<StarDetailDto> findById(String id) {
        return starRepository.findById(id).map(star -> {
            List<MovieRefDto> movies = movieRepository.findByStars_IdOrderByTitleAsc(id).stream()
                    .map(this::toMovieRef)
                    .toList();
            String birthYear = star.getBirthYear() == null ? "" : star.getBirthYear().toString();
            return new StarDetailDto(
                    star.getId(), star.getName(), birthYear,
                    TmdbImageUrls.profile(star.getProfilePath()), movies);
        });
    }

    private MovieRefDto toMovieRef(Movie m) {
        return new MovieRefDto(m.getId(), m.getTitle(), m.getYear(), TmdbImageUrls.poster(m.getPosterPath()));
    }
}
