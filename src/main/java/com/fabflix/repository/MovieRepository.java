package com.fabflix.repository;

import com.fabflix.entity.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovieRepository extends JpaRepository<Movie, String>, JpaSpecificationExecutor<Movie> {

    List<Movie> findByStars_IdOrderByTitleAsc(String starId);
}
