package com.fabflix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Genre ids are TMDb's own fixed genre-list ids (loaded verbatim from
 * TMDb's /genre/movie/list by the seeding script), not database-generated —
 * hence no {@code @GeneratedValue} here.
 */
@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Genre {

    @Id
    private Integer id;

    @Column(nullable = false, length = 32)
    private String name;
}
