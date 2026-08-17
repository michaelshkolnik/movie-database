package com.fabflix.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fabflix.entity.Genre;
import com.fabflix.repository.GenreRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BrowseController.class)
class BrowseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreRepository genreRepository;

    @Test
    void browseReturnsGenreNamesAndTheFullDigitThenLetterList() throws Exception {
        Genre action = new Genre();
        action.setId(28);
        action.setName("Action");
        Genre drama = new Genre();
        drama.setId(18);
        drama.setName("Drama");
        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(List.of(action, drama));

        mockMvc.perform(get("/api/browse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genres[0]").value("Action"))
                .andExpect(jsonPath("$.genres[1]").value("Drama"))
                .andExpect(jsonPath("$.letters.length()").value(36))
                .andExpect(jsonPath("$.letters[0]").value("0"))
                .andExpect(jsonPath("$.letters[9]").value("9"))
                .andExpect(jsonPath("$.letters[10]").value("A"))
                .andExpect(jsonPath("$.letters[35]").value("Z"));
    }
}
