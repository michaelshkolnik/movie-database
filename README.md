# Fabflix (Spring Boot + Hibernate rewrite)

A movie browsing/search catalog, being rewritten from a raw-servlet +
MongoDB project into Spring Boot + Hibernate on MySQL, aimed at being a live,
publicly-deployable portfolio project.

The frontend stays plain HTML/CSS/JS (served as static resources by Spring
Boot) — only the backend is being rebuilt.

## Status

Search/browse/single-movie/single-star are ported: JPA entities mapped to
the schema, Spring Data repositories, dynamic query building via
`Specification`, and `@RestController`s serving the same JSON shapes the
static frontend already expects. See "API" below for the full list.

The dataset itself is sourced from [TMDb](https://www.themoviedb.org/)
(The Movie Database) rather than the original coursework's MySQL/XML/Mongo
lineage — a one-time script (`scripts/seed_tmdb.py`) pulls a curated set of
popular movies, cast, and genres, including posters, backdrops, cast photos,
and overviews. See "Local setup" below: **you need to run this script once
before your first `docker compose up`**, since `db/init/` ships with empty
placeholder seed files, not data.

**Login, shopping cart/checkout, and reCAPTCHA from the original coursework
project are intentionally not being ported.** They were classroom-project
stand-ins (a "does this card id match" checkout, a Jasypt-based login) that
don't fit a live, publicly-deployable portfolio piece, so this app is scoped
to a pure browse/search/detail movie catalog — no accounts, no purchases.
The static frontend's cart/login buttons and the cart/checkout/payment pages
have been removed accordingly.

## Prerequisites

- Java 21
- Maven (a wrapper isn't checked in yet -- run `mvn -N wrapper:wrapper` once
  to generate `./mvnw`/`./mvnw.cmd` if you'd rather not rely on a global
  Maven install)
- Docker + Docker Compose (also used by the test suite -- see "Testing")

## Local setup

1. Copy the env template and fill in values (defaults work as-is for local
   dev, except `TMDB_API_KEY` — get a free one at
   https://www.themoviedb.org/settings/api):

   ```
   cp .env.example .env
   ```

2. Generate the seed data from TMDb (one-time, pure-stdlib Python — no pip
   installs needed). This overwrites `db/init/01_genres.sql` through
   `db/init/05_genres_in_movies.sql` and `db/init/09_ratings.sql`:

   ```
   python3 scripts/seed_tmdb.py
   ```

   Pulls 300 popular movies by default; see the script's docstring for
   `TMDB_MOVIE_COUNT`/`TMDB_MIN_VOTES`/`TMDB_CAST_SIZE` overrides. Takes a
   few minutes — it fetches full detail+credits per movie plus a birth-year
   lookup per unique cast member.

3. Start MySQL (+ Adminer for browsing the DB at http://localhost:8081):

   ```
   docker compose up -d
   ```

   On first start only, this loads the schema and the seed data generated in
   step 2 into a `moviedb` database. If you need to reseed later (e.g. after
   re-running the script with different settings), `docker compose down -v`
   first to wipe the data volume, then `up -d` again.

4. Run the app:

   ```
   mvn spring-boot:run
   ```

5. Check it's up and can see the database:

   ```
   curl http://localhost:8080/api/health
   ```

   Should return `{"app":"up","database":"up","movieCount":300}` (or
   however many rows currently exist).

6. Open http://localhost:8080 for the frontend.

## API

All endpoints return JSON and match the contracts the existing frontend JS
already expects (carried over from the original servlets):

| Endpoint | Purpose |
|---|---|
| `GET /api/movies` | Search/browse movies. Params: `title`, `fulltext`, `year`, `director`, `star`, `genre`, `startsWith`, `orderBy` (`title_rating` \| `rating_title`), `dir` (`asc` \| `desc`, defaults to descending), `n`, `page`. |
| `GET /api/single-movie?id=` | One movie's full detail (genres, stars, rating). |
| `GET /api/single-star?id=` | One star's detail plus the movies they appear in. |
| `GET /api/browse` | Genre list + A-Z/0-9 letter list, for the homepage. |
| `GET /api/autocomplete?query=` | Title suggestions (min 3 chars, up to 10). |
| `GET /api/movies-fulltext?query=` | Title token search, up to 50 results. |
| `GET /api/health` | Liveness/readiness check. |

`title`/`fulltext`/`orderBy`/`dir` quirks (empty `dir` sorting descending,
`fulltext` overriding every other filter, `startsWith=0` meaning "starts
with a digit") are intentionally preserved from the original servlets so
the frontend doesn't need any changes.

## Troubleshooting

**`Access denied for user 'fabflix'@'localhost'` even though the credentials
in `.env` are correct, and `docker exec ... mysql -ufabflix -pfabflix` works
fine from inside the container.**

Something else on your machine is already listening on port 3306 and
intercepting the connection before it reaches the Docker container — most
commonly a native MySQL/MariaDB server installed outside of Docker (e.g. via
Homebrew) running as a background service. Check what's actually bound:

```
lsof -iTCP:3306 -sTCP:LISTEN -n -P
```

If you see a `mysqld` process there in addition to Docker's proxy
(`com.docker...`), that's the culprit. Stop it:

```
brew services list          # find the exact service name
brew services stop mysql    # or whatever name showed up
```

Then re-run `lsof` to confirm only Docker remains on port 3306, and retry
`mvn spring-boot:run`.

You can sanity-check the database itself independently of your host's
networking by logging into Adminer (http://localhost:8081) with Server
`mysql` (the docker-compose service name, not `localhost`), username
`fabflix`, password `fabflix`, database `moviedb` — that path goes through
Docker's internal network and bypasses port 3306 on the host entirely.

**Names/titles come back mangled, e.g. `Böhler` shows up as `BÃ¶hler` or
`GÃ©rard`.**

Classic MySQL double-encoding: the seed SQL files are UTF-8, but without an
explicit charset the client connection used to load them (and potentially
the JDBC connection to query them) defaults to something else, so UTF-8
bytes get reinterpreted as Latin-1 and re-encoded — twice. Fixed by having
`db/init/*.sql` start with `SET NAMES utf8mb4;`, forcing the MySQL server to
default to `utf8mb4` (`docker-compose.yml`'s `command:`), and adding
`useUnicode=true&characterEncoding=UTF-8` to the JDBC URL. If you hit this
before pulling that fix, the bad bytes are already persisted in your data
volume — a config change alone won't retroactively fix rows that already
went in wrong. Reseed:

```
docker compose down -v
docker compose up -d
```

## About the seed data

All movie/star/genre/rating data comes from `scripts/seed_tmdb.py`, which
pulls it live from the TMDb API (see "Local setup" above) — `db/init/`
itself only ships the schema (`00_schema.sql`) plus empty placeholder files
that the script overwrites. There's no data checked into the repo, so
nothing to scrub for secrets or fake PII.

IDs (`movies.id`, `stars.id`, `genres.id`) are TMDb's own numeric IDs, not
the original coursework's IMDb-style `tt.../nm...` identifiers. Poster,
backdrop, and profile images are stored as relative TMDb CDN paths and
expanded to full URLs at the API layer (`TmdbImageUrls`), so nothing
image-related is duplicated between the DB and the app config.

The `customers`, `credit_cards`, `sales`, and `employees` tables from the
original coursework project (and the `add_movie` stored procedure) have
been dropped from the schema entirely — they only ever existed to support
login/cart/checkout, which this project doesn't have.

## Testing

```
mvn test
```

Controller tests (`MovieControllerTest`, `StarControllerTest`,
`BrowseControllerTest`, `HealthControllerTest`) are pure `@WebMvcTest`
slices with mocked services/repositories -- no database needed.

`MovieSpecificationsIT` is different: it spins up a real MySQL container via
Testcontainers (not H2) and exercises `MovieSpecifications` directly,
because the bug it guards against -- MySQL rejecting
`SELECT DISTINCT ... ORDER BY <expr not in the SELECT list>` -- is specific
to MySQL's dialect and wouldn't reproduce against an embedded database. This
means **Docker needs to be running** for `mvn test` to pass, independent of
whatever's in `docker-compose.yml` (Testcontainers manages its own
short-lived container).

## Configuration

All config is via environment variables (see `.env.example`), consumed in
`src/main/resources/application.yml`. Nothing is hardcoded to a machine path
or a specific DB host, unlike the original project.

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `MYSQL_ROOT_PASSWORD` | Only used by docker-compose to bootstrap MySQL |
| `TMDB_API_KEY` | Used by `scripts/seed_tmdb.py` to pull data from TMDb |
| `SERVER_PORT` | Defaults to 8080 |

## Roadmap

1. ~~JPA entities + Spring Data repositories mapped to the existing schema.~~ Done.
2. ~~Port search/browse/autocomplete/single-movie/single-star into `@RestController`s under `/api/...`.~~ Done.
3. ~~TMDb-backed data seeding script and schema redesign; posters/overviews/cast
   photos end-to-end through the API and frontend.~~ Done.
4. Deploy: containerize, managed MySQL, host the app somewhere public.

Explicitly out of scope: login, shopping cart/checkout, reCAPTCHA, and admin
write endpoints — classroom-project artifacts that don't belong in a public
portfolio build. See "Status" above.
