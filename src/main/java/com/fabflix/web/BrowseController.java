package com.fabflix.web;

import com.fabflix.dto.BrowseDto;
import com.fabflix.entity.Genre;
import com.fabflix.repository.GenreRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Replaces BrowseServlet — the genre pill list and A-Z/0-9 letter list on the home page. */
@RestController
@RequestMapping("/api")
public class BrowseController {

    private final GenreRepository genreRepository;

    public BrowseController(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @GetMapping("/browse")
    public BrowseDto browse() {
        List<String> genres = genreRepository.findAllByOrderByNameAsc().stream()
                .map(Genre::getName)
                .toList();

        List<String> letters = new ArrayList<>();
        for (char c = '0'; c <= '9'; c++) {
            letters.add(String.valueOf(c));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            letters.add(String.valueOf(c));
        }

        return new BrowseDto(genres, letters);
    }
}
