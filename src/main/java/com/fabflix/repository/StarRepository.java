package com.fabflix.repository;

import com.fabflix.entity.Star;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StarRepository extends JpaRepository<Star, String> {
}
