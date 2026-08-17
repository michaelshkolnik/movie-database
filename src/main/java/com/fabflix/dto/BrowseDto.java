package com.fabflix.dto;

import java.util.List;

public record BrowseDto(List<String> genres, List<String> letters) {
}
