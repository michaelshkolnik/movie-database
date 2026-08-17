package com.fabflix.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fabflix.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieRepository movieRepository;

    @Test
    void healthReportsUpWhenDatabaseIsReachable() throws Exception {
        when(movieRepository.count()).thenReturn(42L);

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app").value("up"))
                .andExpect(jsonPath("$.database").value("up"))
                .andExpect(jsonPath("$.movieCount").value(42));
    }

    @Test
    void healthReportsDownButStillRespondsWhenRepositoryThrows() throws Exception {
        when(movieRepository.count()).thenThrow(new RuntimeException("connection refused"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app").value("up"))
                .andExpect(jsonPath("$.database").value("down"))
                .andExpect(jsonPath("$.error").value("connection refused"));
    }
}
