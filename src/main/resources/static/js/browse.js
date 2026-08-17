console.log("browse.js loaded");

document.addEventListener("DOMContentLoaded", async () => {
    const genreContainer = document.getElementById("genre-links");
    const letterContainer = document.getElementById("letter-links");

    try {
        const res = await fetch("/api/browse");
        const data = await res.json();

        // Genres — link straight to search results, same as main.html's own
        // embedded browse section (movie-list.html actually reads these
        // params; main.html does not).
        genreContainer.innerHTML = data.genres
            .map(g => `<a href="movie-list.html?genre=${encodeURIComponent(g)}&page=1&n=10">${g}</a>`)
            .join(" | ");

        // Letters and numbers
        letterContainer.innerHTML = data.letters
            .map(l => `<a href="movie-list.html?startsWith=${encodeURIComponent(l)}&page=1&n=10">${l}</a>`)
            .join(" ");
    } catch (err) {
        genreContainer.textContent = "Failed to load genres.";
        letterContainer.textContent = "";
        console.error("Error fetching browse data:", err);
    }
});
