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
