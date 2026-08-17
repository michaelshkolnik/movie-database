let CONTEXT_PATH = window.location.pathname.split("/")[1];
if (CONTEXT_PATH && CONTEXT_PATH.endsWith(".html")) CONTEXT_PATH = "";
const API_BASE = CONTEXT_PATH ? `/${CONTEXT_PATH}/api` : `/api`;

async function fetchJSON(url, options) {
    const res = await fetch(url, options);
    return await res.json();
}

function qs(obj) {
    const u = new URLSearchParams();
    Object.entries(obj).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== "") u.append(k, v);
    });
    return u.toString();
}

function getParams() {
    return new URLSearchParams(window.location.search);
}

function nav(href) {
    window.location.href = href;
}

/* Renders a 5-star widget (rounded to the nearest whole star -- half-star
   unicode glyphs aren't reliably supported across fonts) from a 0-10
   TMDb-style rating, plus the exact score. Shared by movie-list and
   single-movie so the two pages stay visually consistent. */
function starRatingHtml(rating0to10) {
    const r = Number(rating0to10) || 0;
    const outOf5 = Math.max(0, Math.min(5, r / 2));
    const full = Math.round(outOf5);
    const empty = 5 - full;

    const stars = "★".repeat(full) + "☆".repeat(empty);

    return `<span class="star-rating" title="${r.toFixed(1)} / 10">${stars}</span><span class="rating-value">${r.toFixed(1)}</span>`;
}
