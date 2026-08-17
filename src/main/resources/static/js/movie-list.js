const tbody = document.getElementById("movie-table-body");
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

async function loadPage() {
    const q = Object.fromEntries(urlp.entries());
    q.n = q.n || "10";
    q.page = q.page || "1";

    const data = await fetchJSON(`/api/movies?${qs(q)}`);

    tbody.innerHTML = "";

    data.forEach((m) => {
        const tr = document.createElement("tr");

        const title = document.createElement("td");
        const a = document.createElement("a");
        a.href = `single-movie.html?id=${encodeURIComponent(m.id)}`;
        a.textContent = m.title;
        title.appendChild(a);

        const year = document.createElement("td");
        year.textContent = m.year;

        const dir = document.createElement("td");
        dir.textContent = m.director;

        const gens = document.createElement("td");
        gens.innerHTML = (m.genres || []).map(g =>
            `<a href="movie-list.html?${qs({ genre: g, page: 1, n: q.n })}">${g}</a>`).join(", ");

        const stars = document.createElement("td");
        stars.innerHTML = (m.stars || []).map(s =>
            `<a href="single-star.html?id=${encodeURIComponent(s.id)}">${s.name}</a>`).join(", ");

        const rating = document.createElement("td");
        rating.textContent = Number(m.rating || 0).toFixed(1);

        tr.append(title, year, dir, gens, stars, rating);
        tbody.appendChild(tr);
    });

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

loadPage();
