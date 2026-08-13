const msg = document.getElementById("status-message");
const ret = document.getElementById("return-main");
async function placeOrder() {
    const res = await fetchJSON(`${API_BASE}/checkout`, { method: "POST" });
    if (res.status === "success") msg.textContent = "Order placed successfully.";
    else if (res.status === "fail") msg.textContent = res.message || "Checkout failed.";
    else msg.textContent = "An error occurred.";
}
ret.addEventListener("click", () => nav("main.html"));
placeOrder();
