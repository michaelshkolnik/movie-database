# Fabflix (Spring Boot + Hibernate rewrite)

A movie browsing / cart / checkout app, being rewritten from a raw-servlet +
MongoDB project into Spring Boot + Hibernate on MySQL, aimed at being a live,
publicly-deployable portfolio project.

The frontend stays plain HTML/CSS/JS (served as static resources by Spring
Boot) — only the backend is being rebuilt.

## Status

Search/browse/single-movie/single-star are ported: JPA entities mapped to
the existing schema, Spring Data repositories, dynamic query building via
`Specification`, and `@RestController`s serving the same JSON shapes the
static frontend already expects. See "API" below for the full list.

Not yet ported: cart/checkout, login. See "Roadmap".

## Prerequisites

- Java 21
- Maven (or use `./mvnw` once the wrapper is added)
- Docker + Docker Compose

## Local setup

1. Copy the env template and fill in values (defaults work as-is for local dev):

   ```
   cp .env.example .env
   ```

2. Start MySQL (+ Adminer for browsing the DB at http://localhost:8081):

   ```
   docker compose up -d
   ```

   On first start only, this loads the full schema and seed data from
   `db/init/` (movies, stars, genres, ratings, and demo customers/credit
   cards/sales) into a `moviedb` database. If you need to reseed later,
   `docker compose down -v` first to wipe the data volume.

3. Run the app:

   ```
   mvn spring-boot:run
   ```

4. Check it's up and can see the database:

   ```
   curl http://localhost:8080/api/health
   ```

   Should return `{"app":"up","database":"up","movieCount":9052}` (or
   however many rows currently exist).

5. Open http://localhost:8080 for the frontend.

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

## About the seed data

`db/init/` contains SQL dumps carried over from the original project. It's
useful for local development, but worth knowing what's in it:

- Customer records use **plaintext demo passwords** (e.g. `'keyboard'`,
  `'paper'`) — fine for local dev, never treat as real credentials.
- `credit_cards` only stores a name + expiration date behind an id, not an
  actual card number — it was already a mock "does this id match" scheme
  in the original project, not real payment data. The checkout flow will
  be rebuilt as an explicit demo/mock flow (see Roadmap).
- There's a demo `employees` login seeded in (`classta@email.edu` /
  `classta`) from the original coursework project.

Once TMDb-backed seeding is wired up, this movie/star data will likely be
replaced or supplemented with richer, current data (posters, cast photos,
overviews) pulled from the TMDb API.

## Configuration

All config is via environment variables (see `.env.example`), consumed in
`src/main/resources/application.yml`. Nothing is hardcoded to a machine path
or a specific DB host, unlike the original project.

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `MYSQL_ROOT_PASSWORD` | Only used by docker-compose to bootstrap MySQL |
| `TMDB_API_KEY` | Not consumed yet — reserved for the data-seeding step |
| `SERVER_PORT` | Defaults to 8080 |

## Roadmap

1. ~~JPA entities + Spring Data repositories mapped to the existing schema.~~ Done.
2. ~~Port search/browse/autocomplete/single-movie/single-star into `@RestController`s under `/api/...`.~~ Done.
3. Cart/checkout as an explicit mock flow (no real payment data).
4. Spring Security for login, with BCrypt-hashed passwords (replacing the
   original project's Jasypt-based approach).
5. TMDb-backed data seeding script to replace/enrich the current dataset
   with posters, cast photos, and current metadata.
6. Deploy: containerize, managed MySQL, host the app somewhere public.
