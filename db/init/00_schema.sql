DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE movies (
                        id VARCHAR(10) PRIMARY KEY,
                        title VARCHAR(100) NOT NULL,
                        year INT NOT NULL,
                        director VARCHAR(100) NOT NULL
);


CREATE TABLE stars (
                       id VARCHAR(10) PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       birth_year INT NULL
);

CREATE TABLE stars_in_movies (
                                 star_id VARCHAR(10) NOT NULL,
                                 movie_id VARCHAR(10) NOT NULL,
                                 PRIMARY KEY (star_id, movie_id),
                                 FOREIGN KEY (star_id) REFERENCES stars(id),
                                 FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE genres (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(32) NOT NULL
);

CREATE TABLE genres_in_movies (
                                  genre_id INT NOT NULL,
                                  movie_id VARCHAR(10) NOT NULL,
                                  PRIMARY KEY (genre_id, movie_id),
                                  FOREIGN KEY (genre_id) REFERENCES genres(id),
                                  FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE credit_cards (
                              id VARCHAR(20) PRIMARY KEY,
                              first_name VARCHAR(50) NOT NULL,
                              last_name VARCHAR(50) NOT NULL,
                              expiration DATE NOT NULL
);

CREATE TABLE customers (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           first_name VARCHAR(50) NOT NULL,
                           last_name VARCHAR(50) NOT NULL,
                           credit_card_id VARCHAR(20) NOT NULL,
                           address VARCHAR(200) NOT NULL,
                           email VARCHAR(50) NOT NULL,
                           password VARCHAR(20) NOT NULL,
                           FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id)
);


CREATE TABLE sales (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       customer_id INT NOT NULL,
                       movie_id VARCHAR(10) NOT NULL,
                       sale_date DATE NOT NULL,
                       FOREIGN KEY (customer_id) REFERENCES customers(id),
                       FOREIGN KEY (movie_id) REFERENCES movies(id)
);


CREATE TABLE ratings (
                         movie_id VARCHAR(10) PRIMARY KEY,
                         rating FLOAT NOT NULL,
                         vote_count INT NOT NULL,
                         FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE IF NOT EXISTS employees (
                                         email VARCHAR(50) PRIMARY KEY,
                                         password VARCHAR(128) NOT NULL,
                                         fullname VARCHAR(100)
);


INSERT INTO employees (email, password, fullname)
VALUES ('classta@email.edu', 'classta', 'TA CS122B');
