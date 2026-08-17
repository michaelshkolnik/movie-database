SET NAMES utf8mb4;
USE moviedb;

DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie(
    IN m_title VARCHAR(100),
    IN m_year INT,
    IN m_director VARCHAR(100),
    IN s_name VARCHAR(100),
    IN g_name VARCHAR(32),
    OUT msg VARCHAR(255)
)
BEGIN
    DECLARE movieId VARCHAR(10);
    DECLARE starId VARCHAR(10);
    DECLARE genreId INT;
    DECLARE nextMovieNum INT;
    DECLARE nextStarNum INT;

    -- Check if the movie already exists
    SELECT id INTO movieId
    FROM movies
    WHERE title = m_title AND year = m_year AND director = m_director
    LIMIT 1;

    IF movieId IS NOT NULL THEN
        SET msg = CONCAT('Movie "', m_title, '" already exists.');
    ELSE
        -- Generate new movie ID safely
        SELECT IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1
        INTO nextMovieNum
        FROM movies;

        SET movieId = CONCAT('tt', LPAD(nextMovieNum, 7, '0'));

        INSERT INTO movies(id, title, year, director)
        VALUES (movieId, m_title, m_year, m_director);

        -- Find or create genre
        SELECT id INTO genreId FROM genres WHERE name = g_name LIMIT 1;
        IF genreId IS NULL THEN
            INSERT INTO genres(name) VALUES (g_name);
            SET genreId = LAST_INSERT_ID();
        END IF;

        INSERT INTO genres_in_movies(genre_id, movie_id)
        VALUES (genreId, movieId);

        -- Find or create star
        SELECT id INTO starId FROM stars WHERE name = s_name LIMIT 1;
        IF starId IS NULL THEN
            SELECT IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1
            INTO nextStarNum
            FROM stars;

            SET starId = CONCAT('nm', LPAD(nextStarNum, 7, '0'));
            INSERT INTO stars(id, name) VALUES (starId, s_name);
        END IF;

        INSERT INTO stars_in_movies(star_id, movie_id)
        VALUES (starId, movieId);

        SET msg = CONCAT('Movie "', m_title, '" added successfully (ID=', movieId, ').');
    END IF;
END$$

DELIMITER ;
