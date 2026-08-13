const tbodyCart = document.getElementById("cart-body");
const totalEl = document.getElementById("total-price");
const proceedBtn = document.getElementById("proceed-payment");
const backMainBtn = document.getElementById("back-main");
function priceFor(id) {
    let h = 0;
    for (let i = 0; i < id.length; i++) h = (h * 31 + id.charCodeAt(i)) >>> 0;
    return (9.99 + (h % 200) / 10).toFixed(2);
}
async function movieTitle(id) {
    const d = await fetchJSON(`${API_BASE}/single-movie?${qs({ id })}`);
    return d.title || id;
}
async function renderCart() {
    const data = await fetchJSON(`${API_BASE}/cart`);
    tbodyCart.innerHTML = "";
    let total = 0;
    for (const item of data) {
        const tr = document.createElement("tr");
        const tdTitle = document.createElement("td");
        tdTitle.textContent = item.movieId;
        movieTitle(item.movieId).then(t => tdTitle.textContent = t);
        const tdQty = document.createElement("td");
        tdQty.textContent = item.quantity;
        const tdAct = document.createElement("td");
        const inc = document.createElement("button");
        inc.textContent = "+";
        inc.addEventListener("click", async () => {
            await fetchJSON(`${API_BASE}/cart`, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: qs({ movieId: item.movieId, delta: 1 })
            });
            renderCart();
        });
        const dec = document.createElement("button");
        dec.textContent = "–";
        dec.addEventListener("click", async () => {
            await fetchJSON(`${API_BASE}/cart`, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: qs({ movieId: item.movieId, delta: -1 })
            });
            renderCart();
        });
        const del = document.createElement("button");
        del.textContent = "Delete";
        del.addEventListener("click", async () => {
            await fetchJSON(`${API_BASE}/cart`, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: qs({ movieId: item.movieId, delta: -item.quantity })
            });
            renderCart();
        });
        tdAct.append(inc, dec, del);
        tr.append(tdTitle, tdQty, tdAct);
        tbodyCart.appendChild(tr);
        total += parseFloat(priceFor(item.movieId)) * item.quantity;
    }
    totalEl.textContent = total.toFixed(2);
}
proceedBtn.addEventListener("click", () => nav("payment.html"));
backMainBtn.addEventListener("click", () => nav("main.html"));
renderCart();
