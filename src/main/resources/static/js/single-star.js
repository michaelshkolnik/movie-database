const p2 = getParams();
const starId = p2.get("id");
const retQ = p2.get("q");
const nameEl = document.getElementById("star-name");
const birthEl = document.getElementById("star-birth");
const photoEl = document.getElementById("star-photo");
const bodyEl = document.getElementById("star-movie-body");
const backBtn2 = document.getElementById("back-list");

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

    bodyEl.innerHTML = "";
    (data.movies || []).forEach(m => {
        const tr = document.createElement("tr");

        const posterTd = document.createElement("td");
        if (m.posterUrl) {
            const img = document.createElement("img");
            img.className = "poster-thumb";
            img.src = m.posterUrl;
            img.alt = m.title || "";
            img.loading = "lazy";
            img.addEventListener("error", () => {
                img.replaceWith(Object.assign(document.createElement("div"), { className: "poster-placeholder" }));
            });
            posterTd.appendChild(img);
        } else {
            const div = document.createElement("div");
            div.className = "poster-placeholder";
            posterTd.appendChild(div);
        }

        const t1 = document.createElement("td");
        const a = document.createElement("a");
        const qstr = retQ ? `&q=${encodeURIComponent(retQ)}` : "";
        a.href = `single-movie.html?id=${encodeURIComponent(m.id)}${qstr}`;
        a.textContent = m.title || "";
        const t2 = document.createElement("td");
        t2.textContent = m.year || "";
        t1.appendChild(a);
        tr.append(posterTd, t1, t2);
        bodyEl.appendChild(tr);
    });
}
backBtn2.addEventListener("click", () => {
    if (retQ) nav(`movie-list.html${retQ}`);
    else nav("main.html");
});
loadStar();
