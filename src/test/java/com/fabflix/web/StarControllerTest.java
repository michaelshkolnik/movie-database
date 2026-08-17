package com.fabflix.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fabflix.dto.MovieRefDto;
import com.fabflix.dto.StarDetailDto;
import com.fabflix.service.StarService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StarController.class)
class StarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StarService starService;

    @Test
    void singleStarWithoutIdReturnsError() throws Exception {
        mockMvc.perform(get("/api/single-star"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("No ID provided"));
    }

    @Test
    void singleStarNotFoundReturnsError() throws Exception {
        when(starService.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/single-star").param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("No star found"));
    }

    @Test
    void singleStarFoundReturnsStarWithMovies() throws Exception {
        // birthYear is a String on the DTO (mapped to the "birth_year" JSON
        // key via @JsonProperty) -- covers that the legacy field name
        // survived the TMDb rewrite.
        StarDetailDto dto = new StarDetailDto(
                "1", "Marlon Brando", "1924", "/profile.jpg",
                List.of(new MovieRefDto("100", "The Godfather", 1972, "/poster.jpg")));
        when(starService.findById("1")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/single-star").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marlon Brando"))
                .andExpect(jsonPath("$.birth_year").value("1924"))
                .andExpect(jsonPath("$.movies[0].title").value("The Godfather"));
    }
}
