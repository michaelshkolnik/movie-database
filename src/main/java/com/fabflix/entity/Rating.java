package com.fabflix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shares its primary key with {@link Movie} (ratings.movie_id is both the PK
 * and the FK) — modeled as a @MapsId one-to-one rather than giving Rating its
 * own surrogate id, to mirror the schema exactly.
 */
@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "movieId")
public class Rating {

    @Id
    @Column(name = "movie_id", length = 10)
    private String movieId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(nullable = false)
    private Float rating;

    @Column(name = "vote_count", nullable = false)
    private Integer voteCount;
}
