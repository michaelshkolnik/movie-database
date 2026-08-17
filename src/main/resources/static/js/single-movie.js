console.log("single-movie.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    const movieId = params.get("id");

    const titleEl = document.getElementById("movie-title");
    const yearEl = document.getElementById("movie-year");
    const directorEl = document.getElementById("movie-director");
    const ratingEl = document.getElementById("movie-rating");
    const genresEl = document.getElementById("movie-genres");
    const starsEl = document.getElementById("movie-stars");
    const overviewEl = document.getElementById("movie-overview");
    const posterEl = document.getElementById("movie-poster");
    const backdropEl = document.getElementById("movie-backdrop");

    function fetchMovie() {
        fetch(`/api/single-movie?id=${movieId}`)
            .then((res) => res.json())
            .then((data) => {
                console.log("Movie data:", data);

                // data should be a single movie object or an array with one item
                const movie = Array.isArray(data) ? data[0] : data;

                titleEl.textContent = movie.title || "(Untitled)";
                yearEl.textContent = movie.year || "";
                directorEl.textContent = movie.director || "";
                ratingEl.textContent = movie.rating || "N/A";
                overviewEl.textContent = movie.overview || "";

                genresEl.innerHTML = movie.genres
                    ? movie.genres.map((g) => `<span class="pill">${g}</span>`).join(" ")
                    : "None";

                starsEl.innerHTML = movie.stars
                    ? movie.stars
                        .map(
                            (s) =>
                                `<a href="single-star.html?id=${s.id}">${s.name}</a>`
                        )
                        .join(", ")
                    : "None";

                if (movie.posterUrl) {
                    posterEl.src = movie.posterUrl;
                    posterEl.alt = movie.title || "";
                    posterEl.hidden = false;
                    posterEl.addEventListener("error", () => {
                        posterEl.hidden = true;
                    });
                } else {
                    posterEl.hidden = true;
                }

                if (movie.backdropUrl) {
                    backdropEl.style.backgroundImage = `url("${movie.backdropUrl}")`;
                    backdropEl.style.display = "block";
                } else {
                    backdropEl.style.display = "none";
                }
            })
            .catch((err) => console.error("Failed to load movie:", err));
    }

    document.getElementById("back-results").addEventListener("click", () => {
        window.history.back();
    });

    fetchMovie();
});
