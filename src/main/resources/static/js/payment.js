console.log("payment.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("payment-form");
    const msg = document.getElementById("payment-msg");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const data = {
            first_name: form.elements["first_name"].value.trim(),
            last_name: form.elements["last_name"].value.trim(),
            credit_card_id: form.elements["credit_card_id"].value.trim(),
            expiration: form.elements["expiration"].value.trim()
        };

        console.log("Submitting payment data:", data);

        const res = await fetch("/api/checkout", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams(data)
        });

        const result = await res.json();

        if (result.status === "success") {
            msg.textContent = "Payment verified and order placed!";
            setTimeout(() => window.location.href = "checkout.html?status=success", 1000);
        } else {
            msg.textContent = "Payment failed: " + result.message;
        }
    });
});
