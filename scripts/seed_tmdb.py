#!/usr/bin/env python3
"""
Pulls a curated set of popular movies from TMDb (themoviedb.org) and writes
them out as the db/init/*.sql seed files, replacing whatever is currently
there. Run this once (or any time you want a fresh/bigger dataset) from the
project root:

    python3 scripts/seed_tmdb.py

Then reseed the database (the seed files only run on a FRESH data volume):

    docker compose down -v && docker compose up -d

Requires only the Python standard library (no pip installs). Reads
TMDB_API_KEY out of a .env file in the project root -- get a free key at
https://www.themoviedb.org/settings/api.

Configuration (all optional, set as environment variables):
    TMDB_MOVIE_COUNT   how many movies to pull (default 300)
    TMDB_MIN_VOTES     minimum TMDb vote_count for a movie to qualify (default 300)
    TMDB_CAST_SIZE     how many top-billed cast members to keep per movie (default 6)
"""

import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ENV_PATH = os.path.join(PROJECT_ROOT, ".env")
INIT_DIR = os.path.join(PROJECT_ROOT, "db", "init")
API_BASE = "https://api.themoviedb.org/3"

MOVIE_COUNT = int(os.environ.get("TMDB_MOVIE_COUNT", "300"))
MIN_VOTES = int(os.environ.get("TMDB_MIN_VOTES", "300"))
CAST_SIZE = int(os.environ.get("TMDB_CAST_SIZE", "6"))


def load_api_key():
    key = os.environ.get("TMDB_API_KEY")
    if key:
        return key.strip()
    if os.path.exists(ENV_PATH):
        with open(ENV_PATH, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                k, _, v = line.partition("=")
                if k.strip() == "TMDB_API_KEY":
                    v = v.strip().strip('"').strip("'")
                    if v:
                        return v
    sys.exit(
        "No TMDB_API_KEY found. Set it in .env (project root) or as an "
        "environment variable before running this script."
    )


API_KEY = load_api_key()


def api_get(path, params=None, retries=3):
    """GET a TMDb API endpoint and return the parsed JSON body."""
    q = {"api_key": API_KEY, "language": "en-US"}
    if params:
        q.update(params)
    url = f"{API_BASE}{path}?{urllib.parse.urlencode(q)}"

    for attempt in range(1, retries + 1):
        try:
            req = urllib.request.Request(url, headers={"Accept": "application/json"})
            with urllib.request.urlopen(req, timeout=20) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            if e.code == 429:
                wait = float(e.headers.get("Retry-After", "1"))
                print(f"  rate limited, waiting {wait}s...", file=sys.stderr)
                time.sleep(wait)
                continue
            if e.code == 404:
                return None
            if attempt == retries:
                print(f"  giving up on {path} after {attempt} attempts: {e}", file=sys.stderr)
                return None
            time.sleep(1.0 * attempt)
        except (urllib.error.URLError, TimeoutError, ConnectionError) as e:
            if attempt == retries:
                print(f"  giving up on {path} after {attempt} attempts: {e}", file=sys.stderr)
                return None
            time.sleep(1.0 * attempt)
    return None


def sql_str(value):
    if value is None:
        return "NULL"
    # MySQL's default sql_mode treats backslash as an escape character inside
    # string literals (even though we quote with '' rather than \'), so a
    # stray backslash in an overview/title -- or one immediately before the
    # closing quote -- can otherwise corrupt the literal or the statement.
    # Escape backslashes first, then double any single quotes.
    s = str(value).replace("\\", "\\\\").replace("'", "''")
    return "'" + s + "'"


def sql_num(value):
    return "NULL" if value is None else str(value)


def write_sql(filename, table, columns, rows, chunk_size=200):
    """Write chunked multi-row INSERT statements for `rows` (list of tuples,
    already SQL-literal strings) into db/init/<filename>."""
    path = os.path.join(INIT_DIR, filename)
    with open(path, "w", encoding="utf-8") as f:
        f.write("SET NAMES utf8mb4;\n")
        for i in range(0, len(rows), chunk_size):
            chunk = rows[i:i + chunk_size]
            f.write(f"INSERT INTO {table} ({', '.join(columns)}) VALUES\n")
            f.write(",\n".join("(" + ", ".join(r) + ")" for r in chunk))
            f.write(";\n")
    print(f"wrote {path} ({len(rows)} rows)")


def year_from_release_date(release_date):
    if not release_date or len(release_date) < 4:
        return None
    try:
        return int(release_date[:4])
    except ValueError:
        return None


def birth_year_from_birthday(birthday):
    if not birthday:
        return None
    m = re.match(r"^(\d{4})-\d{2}-\d{2}$", birthday)
    return int(m.group(1)) if m else None


def main():
    os.makedirs(INIT_DIR, exist_ok=True)

    print("Fetching official genre list...")
    genre_list = api_get("/genre/movie/list")
    if not genre_list or "genres" not in genre_list:
        sys.exit("Could not fetch genre list -- check TMDB_API_KEY and network access.")
    genres = genre_list["genres"]  # [{id, name}, ...]
    genre_ids_known = {g["id"] for g in genres}

    print(f"Discovering up to {MOVIE_COUNT} popular movies (min {MIN_VOTES} votes)...")
    discovered = []
    seen_ids = set()
    page = 1
    while len(discovered) < MOVIE_COUNT and page <= 500:
        result = api_get("/discover/movie", {
            "sort_by": "popularity.desc",
            "include_adult": "false",
            "include_video": "false",
            "vote_count.gte": str(MIN_VOTES),
            "page": str(page),
        })
        if not result or not result.get("results"):
            break
        for m in result["results"]:
            if m["id"] in seen_ids:
                continue
            if not m.get("release_date"):
                continue
            seen_ids.add(m["id"])
            discovered.append(m)
            if len(discovered) >= MOVIE_COUNT:
                break
        page += 1
        if page > result.get("total_pages", page):
            break
    print(f"  discovered {len(discovered)} candidate movies")

    movies_rows = []
    stars_by_id = {}          # tmdb person id -> {"name", "profile_path", "birth_year" (filled later)}
    stars_in_movies_rows = []
    genres_in_movies_rows = []
    ratings_rows = []
    genre_ids_used = set()

    for idx, stub in enumerate(discovered, start=1):
        movie_id = stub["id"]
        detail = api_get(f"/movie/{movie_id}", {"append_to_response": "credits"})
        if not detail:
            continue

        year = year_from_release_date(detail.get("release_date"))
        if year is None:
            continue

        crew = detail.get("credits", {}).get("crew", [])
        directors = [c["name"] for c in crew if c.get("job") == "Director"]
        director = ", ".join(directors) if directors else "Unknown"

        title = (detail.get("title") or stub.get("title") or "").strip()
        if not title:
            continue

        movies_rows.append((
            sql_str(str(movie_id)),
            sql_str(title[:200]),
            sql_num(year),
            sql_str(director[:200]),
            sql_str(detail.get("overview") or None),
            sql_str(detail.get("poster_path")),
            sql_str(detail.get("backdrop_path")),
        ))

        ratings_rows.append((
            sql_str(str(movie_id)),
            sql_num(detail.get("vote_average", 0.0)),
            sql_num(detail.get("vote_count", 0)),
        ))

        for g in detail.get("genres", []):
            if g["id"] in genre_ids_known:
                genre_ids_used.add(g["id"])
                genres_in_movies_rows.append((sql_num(g["id"]), sql_str(str(movie_id))))

        cast = detail.get("credits", {}).get("cast", [])
        cast_sorted = sorted(cast, key=lambda c: c.get("order", 9999))[:CAST_SIZE]
        for c in cast_sorted:
            star_id = c["id"]
            if star_id not in stars_by_id:
                stars_by_id[star_id] = {
                    "name": (c.get("name") or "").strip()[:150],
                    "profile_path": c.get("profile_path"),
                    "birth_year": None,
                }
            stars_in_movies_rows.append((sql_str(str(star_id)), sql_str(str(movie_id))))

        if idx % 25 == 0 or idx == len(discovered):
            print(f"  processed {idx}/{len(discovered)} movies, {len(stars_by_id)} unique cast so far")

    print(f"Fetching birth years for {len(stars_by_id)} unique cast members...")
    for count, star_id in enumerate(list(stars_by_id.keys()), start=1):
        person = api_get(f"/person/{star_id}")
        if person:
            stars_by_id[star_id]["birth_year"] = birth_year_from_birthday(person.get("birthday"))
            if not stars_by_id[star_id]["name"]:
                stars_by_id[star_id]["name"] = (person.get("name") or "Unknown").strip()[:150]
        if count % 100 == 0 or count == len(stars_by_id):
            print(f"  {count}/{len(stars_by_id)} people done")

    stars_rows = []
    for star_id, info in stars_by_id.items():
        name = info["name"] or "Unknown"
        stars_rows.append((
            sql_str(str(star_id)),
            sql_str(name),
            sql_num(info["birth_year"]),
            sql_str(info["profile_path"]),
        ))

    # Defensive de-dup in case the API ever returns an overlapping cast/genre
    # entry for the same movie -- avoids a PRIMARY KEY collision on insert.
    stars_in_movies_rows = sorted(set(stars_in_movies_rows))
    genres_in_movies_rows = sorted(set(genres_in_movies_rows))

    genres_rows = [(sql_num(g["id"]), sql_str(g["name"][:32])) for g in genres if g["id"] in genre_ids_used]

    write_sql("01_genres.sql", "genres", ["id", "name"], genres_rows)
    write_sql("02_movies.sql", "movies",
              ["id", "title", "year", "director", "overview", "poster_path", "backdrop_path"],
              movies_rows)
    write_sql("03_stars.sql", "stars", ["id", "name", "birth_year", "profile_path"], stars_rows)
    write_sql("04_stars_in_movies.sql", "stars_in_movies", ["star_id", "movie_id"], stars_in_movies_rows)
    write_sql("05_genres_in_movies.sql", "genres_in_movies", ["genre_id", "movie_id"], genres_in_movies_rows)
    write_sql("09_ratings.sql", "ratings", ["movie_id", "rating", "vote_count"], ratings_rows)

    print("\nDone. Reseed with:  docker compose down -v && docker compose up -d")


if __name__ == "__main__":
    main()
