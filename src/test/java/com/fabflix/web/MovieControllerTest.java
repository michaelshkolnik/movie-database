package com.fabflix.web;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fabflix.dto.AutocompleteSuggestionDto;
import com.fabflix.dto.MovieBriefDto;
import com.fabflix.dto.MovieSearchParams;
import com.fabflix.dto.MovieSummaryDto;
import com.fabflix.service.MovieService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Covers the request-parsing quirks in MovieController that the frontend
 * relies on but that aren't obvious from reading the code once: unparsable
 * numeric params silently fall back to defaults rather than 400ing, n/page
 * are clamped to a minimum of 1 (which is NOT the same as "falls back to
 * the default"), and autocomplete/movies-fulltext short-circuit before
 * ever calling the service.
 */
@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    void moviesFallsBackToDefaultsOnUnparsableNumericParams() throws Exception {
        when(movieService.search(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/movies")
                        .param("year", "not-a-number")
                        .param("n", "abc")
                        .param("page", "xyz"))
                .andExpect(status().isOk());

        ArgumentCaptor<MovieSearchParams> captor = ArgumentCaptor.forClass(MovieSearchParams.class);
        verify(movieService).search(captor.capture());
        MovieSearchParams params = captor.getValue();

        assertNull(params.year());
        assertEquals(10, params.n());
        assertEquals(1, params.page());
    }

    @Test
    void moviesClampsNonPositiveNAndPageToOneRatherThanDefaulting() throws Exception {
        when(movieService.search(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/movies").param("n", "0").param("page", "-5"))
                .andExpect(status().isOk());

        ArgumentCaptor<MovieSearchParams> captor = ArgumentCaptor.forClass(MovieSearchParams.class);
        verify(movieService).search(captor.capture());
        MovieSearchParams params = captor.getValue();

        // "0"/"-5" parse successfully, so these clamp to 1 -- they do NOT
        // fall back to the n=10/page=1 defaults the way an unparsable
        // string would.
        assertEquals(1, params.n());
        assertEquals(1, params.page());
    }

    @Test
    void moviesReturnsWhatTheServiceReturns() throws Exception {
        MovieSummaryDto dto = new MovieSummaryDto(
                "1", "The Godfather", 1972, "Francis Ford Coppola", 9.2,
                "overview", "/poster.jpg", "/backdrop.jpg", List.of("Drama"), List.of());
        when(movieService.search(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("The Godfather"));
    }

    @Test
    void singleMovieWithoutIdReturnsError() throws Exception {
        mockMvc.perform(get("/api/single-movie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("No ID provided"));
    }

    @Test
    void singleMovieNotFoundReturnsError() throws Exception {
        when(movieService.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/single-movie").param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("No movie found"));
    }

    @Test
    void singleMovieFoundReturnsMovie() throws Exception {
        MovieSummaryDto dto = new MovieSummaryDto(
                "1", "The Godfather", 1972, "Francis Ford Coppola", 9.2,
                "overview", null, null, List.of("Drama"), List.of());
        when(movieService.findById("1")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/single-movie").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Godfather"));
    }

    @Test
    void autocompleteRequiresAtLeastThreeChars() throws Exception {
        mockMvc.perform(get("/api/autocomplete").param("query", "ab"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(movieService, never()).findTitleMatches(any(), anyInt());
    }

    @Test
    void autocompleteMapsTitleMatchesToSuggestions() throws Exception {
        when(movieService.findTitleMatches("god", 10))
                .thenReturn(List.of(new MovieBriefDto("1", "The Godfather", 1972, "Coppola", null)));

        mockMvc.perform(get("/api/autocomplete").param("query", "god"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("The Godfather"))
                .andExpect(jsonPath("$[0].data").value("1"));
    }

    @Test
    void moviesFulltextWithBlankQueryReturnsEmptyWithoutCallingService() throws Exception {
        mockMvc.perform(get("/api/movies-fulltext").param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(movieService, never()).findTitleMatches(any(), anyInt());
    }
}
