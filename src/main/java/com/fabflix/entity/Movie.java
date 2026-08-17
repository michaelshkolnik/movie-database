package com.fabflix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Movie {

    // TMDb's own numeric movie id, as a string.
    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, length = 200)
    private String director;

    @Lob
    @Column
    private String overview;

    // Relative TMDb image path (e.g. "/abc123.jpg"), not a full URL — the
    // API layer combines this with TMDb's image CDN base URL. Null when
    // TMDb has no poster/backdrop for a given movie.
    @Column(name = "poster_path", length = 255)
    private String posterPath;

    @Column(name = "backdrop_path", length = 255)
    private String backdropPath;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "stars_in_movies",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "star_id"))
    @BatchSize(size = 50)
    private Set<Star> stars = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "genres_in_movies",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @BatchSize(size = 50)
    private Set<Genre> genres = new LinkedHashSet<>();

    // Inverse side of Rating's shared-PK @OneToOne. May be null — not every
    // movie in the seed data necessarily has a ratings row.
    @OneToOne(mappedBy = "movie", fetch = FetchType.LAZY)
    private Rating ratingInfo;
}
