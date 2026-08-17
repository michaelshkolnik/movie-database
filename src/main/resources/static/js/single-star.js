const p2 = getParams();
const starId = p2.get("id");
const retQ = p2.get("q");
const nameEl = document.getElementById("star-name");
const birthEl = document.getElementById("star-birth");
const photoEl = document.getElementById("star-photo");
const gridEl = document.getElementById("star-movie-grid");
const backBtn2 = document.getElementById("back-list");

function posterMarkup(posterUrl, title) {
    if (posterUrl) {
        return `<img class="poster-thumb" src="${posterUrl}" alt="${title || ""}" loading="lazy">`;
    }
    return `<div class="poster-placeholder"></div>`;
}

function buildMovieCard(m) {
    const card = document.createElement("div");
    card.className = "movie-card";

    const posterWrap = document.createElement("div");
    posterWrap.className = "movie-card__poster-wrap";
    posterWrap.innerHTML = posterMarkup(m.posterUrl, m.title);
    const posterImg = posterWrap.querySelector("img");
    if (posterImg) {
        posterImg.addEventListener("error", () => {
            posterWrap.innerHTML = `<div class="poster-placeholder"></div>`;
        });
    }

    const body = document.createElement("div");
    body.className = "movie-card__body";

    const qstr = retQ ? `&q=${encodeURIComponent(retQ)}` : "";
    const href = `single-movie.html?id=${encodeURIComponent(m.id)}${qstr}`;

    const titleLink = document.createElement("a");
    titleLink.className = "movie-card__title";
    titleLink.href = href;
    titleLink.textContent = m.title || "";

    const meta = document.createElement("div");
    meta.className = "movie-card__meta";
    meta.textContent = m.year || "";

    body.append(titleLink, meta);
    card.append(posterWrap, body);

    card.addEventListener("click", (e) => {
        if (e.target.closest("a")) return;
        nav(href);
    });

    return card;
}

async function loadStar() {
    const data = await fetchJSON(`${API_BASE}/single-star?${qs({ id: starId })}`);
    nameEl.textContent = data.name || "";
    birthEl.textContent = data.birth_year || "";

    if (data.profileUrl) {
        photoEl.src = data.profileUrl;
        photoEl.alt = data.name || "";
        photoEl.hidden = false;
        photoEl.addEventListener("error", () => {
            photoEl.hidden = true;
        });
    } else {
        photoEl.hidden = true;
    }

    gridEl.innerHTML = "";
    (data.movies || []).forEach(m => gridEl.appendChild(buildMovieCard(m)));
}
backBtn2.addEventListener("click", () => {
    if (retQ) nav(`movie-list.html${retQ}`);
    else nav("main.html");
});
loadStar();
