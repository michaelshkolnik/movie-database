package com.fabflix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stars")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Star {

    // TMDb's own numeric person id, as a string.
    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "birth_year")
    private Integer birthYear;

    // Relative TMDb image path, same convention as Movie.posterPath. Null
    // when TMDb has no headshot on file for this person.
    @Column(name = "profile_path", length = 255)
    private String profilePath;
}
