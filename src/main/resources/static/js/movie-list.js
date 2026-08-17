const grid = document.getElementById("movie-grid");
const prevBtn = document.getElementById("prev-page");
const nextBtn = document.getElementById("next-page");
const pageInfo = document.getElementById("page-info");
const pageSize = document.getElementById("page-size");
const sortBy = document.getElementById("sort-by");
const backMain = document.getElementById("back-main");

const urlp = getParams();

function parseSort() {
    const v = sortBy.value;
    if (v === "title_rating_asc") return { orderBy: "title_rating", dir: "asc" };
    if (v === "title_rating_desc") return { orderBy: "title_rating", dir: "desc" };
    if (v === "rating_title_asc") return { orderBy: "rating_title", dir: "asc" };
    return { orderBy: "rating_title", dir: "desc" };
}

function setSortFromParams() {
    const ob = urlp.get("orderBy") || "title_rating";
    const dir = urlp.get("dir") || "asc";

    if (ob === "title_rating" && dir === "asc") sortBy.value = "title_rating_asc";
    else if (ob === "title_rating" && dir === "desc") sortBy.value = "title_rating_desc";
    else if (ob === "rating_title" && dir === "asc") sortBy.value = "rating_title_asc";
    else sortBy.value = "rating_title_desc";
}

function posterMarkup(posterUrl, title) {
    if (posterUrl) {
        return `<img class="poster-thumb" src="${posterUrl}" alt="${title || ""}" loading="lazy">`;
    }
    return `<div class="poster-placeholder"></div>`;
}

function buildCard(m, pageSizeValue) {
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

    const titleLink = document.createElement("a");
    titleLink.className = "movie-card__title";
    titleLink.href = `single-movie.html?id=${encodeURIComponent(m.id)}`;
    titleLink.textContent = m.title;

    const meta = document.createElement("div");
    meta.className = "movie-card__meta";
    meta.textContent = [m.year, m.director].filter(Boolean).join(" · ");

    const rating = document.createElement("div");
    rating.innerHTML = starRatingHtml(m.rating);

    const genres = document.createElement("div");
    genres.className = "movie-card__genres";
    genres.innerHTML = (m.genres || []).map(g =>
        `<a href="movie-list.html?${qs({ genre: g, page: 1, n: pageSizeValue })}">${g}</a>`).join(", ");

    const stars = document.createElement("div");
    stars.className = "movie-card__stars";
    stars.innerHTML = (m.stars || []).map(s =>
        `<a href="single-star.html?id=${encodeURIComponent(s.id)}">${s.name}</a>`).join(", ");

    body.append(titleLink, meta, rating, genres, stars);
    card.append(posterWrap, body);

    card.addEventListener("click", (e) => {
        if (e.target.closest("a")) return; // let nested genre/star links behave normally
        nav(`single-movie.html?id=${encodeURIComponent(m.id)}`);
    });

    return card;
}

async function loadPage() {
    const q = Object.fromEntries(urlp.entries());
    q.n = q.n || "10";
    q.page = q.page || "1";

    const data = await fetchJSON(`/api/movies?${qs(q)}`);

    grid.innerHTML = "";
    data.forEach((m) => grid.appendChild(buildCard(m, q.n)));

    const p = parseInt(q.page, 10);
    pageInfo.textContent = `Page ${p}`;
    prevBtn.disabled = p <= 1;
    nextBtn.disabled = data.length < parseInt(q.n, 10);
}

prevBtn.addEventListener("click", () => {
    const p = parseInt(urlp.get("page") || "1", 10);
    urlp.set("page", p - 1);
    location.search = urlp.toString();
});

nextBtn.addEventListener("click", () => {
    const p = parseInt(urlp.get("page") || "1", 10);
    urlp.set("page", p + 1);
    location.search = urlp.toString();
});

pageSize.addEventListener("change", () => {
    urlp.set("n", pageSize.value);
    urlp.set("page", "1");
    location.search = urlp.toString();
});

sortBy.addEventListener("change", () => {
    const s = parseSort();
    urlp.set("orderBy", s.orderBy);
    urlp.set("dir", s.dir);
    urlp.set("page", "1");
    location.search = urlp.toString();
});

backMain.addEventListener("click", () => nav("main.html"));

setSortFromParams();
loadPage();
