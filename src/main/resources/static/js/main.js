const genreList = document.getElementById("genre-list");
const letterList = document.getElementById("letter-list");
const searchForm = document.getElementById("search-form");
const cartBtn = document.getElementById("cart-btn");
const logoutBtn = document.getElementById("logout-btn");

const titleInput = document.getElementById("title-input");
const acBox = document.getElementById("autocomplete-box");

async function loadBrowse() {
    const data = await fetchJSON(`${API_BASE}/browse`);

    // GENRES
    const gFrag = document.createDocumentFragment();
    (data.genres || []).forEach((g) => {
        const a = document.createElement("a");
        a.href = `movie-list.html?${qs({ genre: g, page: 1, n: 10 })}`;
        a.textContent = g;
        a.className = "pill";
        gFrag.appendChild(a);
    });
    genreList.innerHTML = "";
    genreList.appendChild(gFrag);

    // LETTERS
    const lFrag = document.createDocumentFragment();
    (data.letters || []).forEach((ch) => {
        const a = document.createElement("a");
        a.href = `movie-list.html?${qs({
            startsWith: ch,
            page: 1,
            n: 10
        })}`;
        a.textContent = ch;
        a.className = "pill";
        lFrag.appendChild(a);
    });

    letterList.innerHTML = "";
    letterList.appendChild(lFrag);
}

/* ===============================
     AUTOCOMPLETE WITH KEYBOARD NAV
================================ */
let acTimeout = null;
let acIndex = -1; // currently highlighted element
let acResults = []; // store suggestions

titleInput.addEventListener("input", () => {
    const q = titleInput.value.trim();
    acBox.innerHTML = "";
    acIndex = -1;
    acResults = [];

    if (q.length < 2) return;

    clearTimeout(acTimeout);
    acTimeout = setTimeout(async () => {
        const results = await fetchJSON(`/api/autocomplete?query=${encodeURIComponent(q)}`);

        acResults = results;
        acBox.innerHTML = "";

        results.forEach((m, i) => {
            const div = document.createElement("div");
            div.className = "ac-item";
            div.textContent = m.value;

            div.addEventListener("click", () => {
                titleInput.value = m.value;
                acBox.innerHTML = "";
            });

            acBox.appendChild(div);
        });

    }, 150);
});

/* Highlight an item */
function updateHighlight() {
    const items = acBox.querySelectorAll(".ac-item");
    items.forEach((el, idx) => {
        el.classList.toggle("ac-highlighted", idx === acIndex);
    });
}

/* Keyboard support */
titleInput.addEventListener("keydown", (e) => {
    const items = acBox.querySelectorAll(".ac-item");
    if (items.length === 0) return;

    if (e.key === "ArrowDown") {
        e.preventDefault();
        acIndex = (acIndex + 1) % items.length;
        updateHighlight();
    } else if (e.key === "ArrowUp") {
        e.preventDefault();
        acIndex = (acIndex - 1 + items.length) % items.length;
        updateHighlight();
    } else if (e.key === "Enter") {
        if (acIndex >= 0 && acIndex < items.length) {
            e.preventDefault();
            const selected = acResults[acIndex];
            titleInput.value = selected.value;
            acBox.innerHTML = "";
        }
    }
});

/* hide autocomplete on outside click */
document.addEventListener("click", (e) => {
    if (!acBox.contains(e.target) && e.target !== titleInput) {
        acBox.innerHTML = "";
        acIndex = -1;
        acResults = [];
    }
});


/* SEARCH */
searchForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const f = new FormData(searchForm);

    const title = f.get("title") || "";

    if (title.trim().split(/\s+/).length > 1) {
        nav(`movie-list.html?${qs({ fulltext: title.trim(), page: 1, n: 10 })}`);
    } else {
        nav(`movie-list.html?${qs({
            title,
            year: f.get("year") || "",
            director: f.get("director") || "",
            star: f.get("star") || "",
            page: 1,
            n: 10
        })}`);
    }
});

cartBtn.addEventListener("click", () => nav("cart.html"));
logoutBtn.addEventListener("click", () => nav("login.html"));

loadBrowse();
