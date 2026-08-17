SET NAMES utf8mb4;
DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;
USE moviedb;

-- Schema sourced from TMDb (themoviedb.org) rather than the original
-- coursework's MySQL/XML/Mongo lineage. IDs are TMDb's own numeric IDs
-- (stored as strings), not the old IMDb-style tt.../nm... identifiers.
-- Cart/checkout/login-only tables (credit_cards, customers, sales,
-- employees) and the add_movie admin stored procedure from the original
-- project are dropped entirely -- unused since login/cart/checkout were
-- cut, and don't fit a from-scratch TMDb-sourced dataset anyway.

CREATE TABLE movies (
                        id VARCHAR(20) PRIMARY KEY,
                        title VARCHAR(200) NOT NULL,
                        year INT NOT NULL,
                        director VARCHAR(200) NOT NULL,
                        overview TEXT NULL,
                        poster_path VARCHAR(255) NULL,
                        backdrop_path VARCHAR(255) NULL
);

CREATE TABLE stars (
                       id VARCHAR(20) PRIMARY KEY,
                       name VARCHAR(150) NOT NULL,
                       birth_year INT NULL,
                       profile_path VARCHAR(255) NULL
);

CREATE TABLE stars_in_movies (
                                 star_id VARCHAR(20) NOT NULL,
                                 movie_id VARCHAR(20) NOT NULL,
                                 PRIMARY KEY (star_id, movie_id),
                                 FOREIGN KEY (star_id) REFERENCES stars(id),
                                 FOREIGN KEY (movie_id) REFERENCES movies(id)
);

-- Genre IDs are TMDb's own fixed genre-list IDs, not auto-generated --
-- the seeding script loads them directly from TMDb's /genre/movie/list.
CREATE TABLE genres (
                        id INT PRIMARY KEY,
                        name VARCHAR(32) NOT NULL
);

CREATE TABLE genres_in_movies (
                                  genre_id INT NOT NULL,
                                  movie_id VARCHAR(20) NOT NULL,
                                  PRIMARY KEY (genre_id, movie_id),
                                  FOREIGN KEY (genre_id) REFERENCES genres(id),
                                  FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE ratings (
                         movie_id VARCHAR(20) PRIMARY KEY,
                         rating FLOAT NOT NULL,
                         vote_count INT NOT NULL,
                         FOREIGN KEY (movie_id) REFERENCES movies(id)
);
