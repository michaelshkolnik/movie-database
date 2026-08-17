package com.fabflix.repository;

import com.fabflix.entity.Genre;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    Optional<Genre> findByName(String name);

    List<Genre> findAllByOrderByNameAsc();
}
