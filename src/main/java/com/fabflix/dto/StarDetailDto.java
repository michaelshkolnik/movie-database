package com.fabflix.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StarDetailDto(
        String id,
        String name,
        @JsonProperty("birth_year") String birthYear,
        String profileUrl,
        List<MovieRefDto> movies) {
}
