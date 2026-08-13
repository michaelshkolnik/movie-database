const p2 = getParams();
const starId = p2.get("id");
const retQ = p2.get("q");
const nameEl = document.getElementById("star-name");
const birthEl = document.getElementById("star-birth");
const bodyEl = document.getElementById("star-movie-body");
const backBtn2 = document.getElementById("back-list");
const cartBtn2 = document.getElementById("cart-btn");
async function loadStar() {
    const data = await fetchJSON(`${API_BASE}/single-star?${qs({ id: starId })}`);
    nameEl.textContent = data.name || "";
    birthEl.textContent = data.birth_year || "";
    bodyEl.innerHTML = "";
    (data.movies || []).forEach(m => {
        const tr = document.createElement("tr");
        const t1 = document.createElement("td");
        const a = document.createElement("a");
        const qstr = retQ ? `&q=${encodeURIComponent(retQ)}` : "";
        a.href = `single-movie.html?id=${encodeURIComponent(m.id)}${qstr}`;
        a.textContent = m.title || "";
        const t2 = document.createElement("td");
        t2.textContent = m.year || "";
        t1.appendChild(a);
        tr.append(t1, t2);
        bodyEl.appendChild(tr);
    });
}
backBtn2.addEventListener("click", () => {
    if (retQ) nav(`movie-list.html${retQ}`);
    else nav("main.html");
});
cartBtn2.addEventListener("click", () => nav("cart.html"));
loadStar();
