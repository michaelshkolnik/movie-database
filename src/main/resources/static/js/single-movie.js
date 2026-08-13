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
            })
            .catch((err) => console.error("Failed to load movie:", err));
    }

    document.getElementById("back-results").addEventListener("click", () => {
        window.history.back();
    });

    document.getElementById("checkout-btn").addEventListener("click", () => {
        window.location.href = "cart.html";
    });

    document.getElementById("add-cart").addEventListener("click", async () => {
        try {
            const res = await fetch(`/api/cart`, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ movieId, delta: "1" }),
            });

            if (res.ok) {
                alert(`Added "${titleEl.textContent}" to cart!`);
            } else {
                alert("Failed to add to cart");
            }
        } catch (err) {
            console.error("Add to cart failed:", err);
            alert("Network error adding to cart.");
        }
    });


    fetchMovie();
});
