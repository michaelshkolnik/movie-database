package com.fabflix.web;

import com.fabflix.dto.ErrorDto;
import com.fabflix.service.StarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Replaces SingleStarServlet. */
@RestController
@RequestMapping("/api")
public class StarController {

    private final StarService starService;

    public StarController(StarService starService) {
        this.starService = starService;
    }

    @GetMapping("/single-star")
    public Object singleStar(@RequestParam(required = false) String id) {
        if (id == null || id.isBlank()) {
            return new ErrorDto("No ID provided");
        }
        return starService.findById(id).<Object>map(s -> s).orElseGet(() -> new ErrorDto("No star found"));
    }
}
