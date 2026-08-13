console.log("browse.js loaded");

document.addEventListener("DOMContentLoaded", async () => {
    const genreContainer = document.getElementById("genre-links");
    const letterContainer = document.getElementById("letter-links");

    try {
        const res = await fetch("/api/browse");
        const data = await res.json();

        // Genres
        genreContainer.innerHTML = data.genres
            .map(g => `<a href="main.html?genre=${encodeURIComponent(g)}">${g}</a>`)
            .join(" | ");

        // Letters and numbers
        letterContainer.innerHTML = data.letters
            .map(l => `<a href="main.html?startsWith=${encodeURIComponent(l)}">${l}</a>`)
            .join(" ");
    } catch (err) {
        genreContainer.textContent = "Failed to load genres.";
        letterContainer.textContent = "";
        console.error("Error fetching browse data:", err);
    }
});
