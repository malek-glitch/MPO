// Meilleurs Prix Ordinateur — shared API client & helpers
const API_BASE = "http://localhost:8080/api";

async function api(path, opts = {}) {
  const headers = Object.assign({}, opts.headers);
  if (opts.body && !(opts.body instanceof FormData)) headers["Content-Type"] = "application/json";
  const token = localStorage.getItem("pcecom_token");
  if (token) headers["Authorization"] = "Bearer " + token;
  const res = await fetch(API_BASE + path, Object.assign({}, opts, { headers }));
  if (res.status === 401 && token) window.dispatchEvent(new Event("auth-expired"));
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error((data && data.error) || res.statusText);
  return data;
}

function money(n) {
  if (n == null) return "";
  return n.toLocaleString("fr-FR") + " TND";
}

function qs(params) {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === "") return;
    p.set(k, v);
  });
  return p.toString();
}

function flattenCategories(nodes, depth = 0, out = []) {
  for (const n of nodes) {
    out.push({ id: n.id, slug: n.slug, name: n.name, label: " ".repeat(depth * 2) + n.name, depth, children: n.children || [] });
    flattenCategories(n.children || [], depth + 1, out);
  }
  return out;
}

function findCategoryPath(tree, slug, trail = []) {
  for (const n of tree) {
    const next = trail.concat(n);
    if (n.slug === slug) return next;
    const found = findCategoryPath(n.children || [], slug, next);
    if (found) return found;
  }
  return null;
}

const CONDITION_LABEL = {
  like_new: "Comme neuf",
  very_good: "Très bon",
  good: "Bon",
  fair: "Correct",
};
const CONDITION_CLASS = {
  like_new: "c-comme-neuf",
  very_good: "c-tres-bon",
  good: "c-bon",
  fair: "c-correct",
};

function conditionBadgeHtml(p) {
  if (!p.isUsed) return '<span class="badge neuf">Neuf</span>';
  const cond = p.used ? p.used.condition : null;
  const label = CONDITION_LABEL[cond] || "Occasion";
  const cls = CONDITION_CLASS[cond] || "c-correct";
  return `<span class="badge occ"><span class="dot ${cls}"></span>Occasion · ${label}</span>`;
}

function specLine(p) {
  const parts = [];
  if (p.processor) parts.push(p.processor);
  if (p.ramGb) parts.push(p.ramGb + " Go RAM");
  if (p.storageGb) parts.push(p.storageGb + " Go " + (p.storageType || "SSD"));
  if (p.screenInches) parts.push(p.screenInches + '"');
  return parts.join(" · ");
}

function waLink(store, product) {
  const number = (store && store.whatsappNumber) || "";
  const template = (store && store.whatsappTemplate) ||
    "Bonjour, je suis intéressé(e) par : {product} ({price} DT) — {link}";
  let text;
  if (product) {
    const productName = `${product.brand} ${product.model}`;
    text = template
      .replaceAll("{product}", productName)
      .replaceAll("{price}", String(product.priceTnd))
      .replaceAll("{link}", location.origin + location.pathname.replace(/[^/]*$/, "") + "product.html?slug=" + product.slug);
  } else {
    text = "Bonjour, je suis intéressé(e) par vos ordinateurs.";
  }
  return `https://wa.me/${number}?text=${encodeURIComponent(text)}`;
}

function toast(msg) {
  let t = document.getElementById("toast");
  if (!t) {
    t = document.createElement("div");
    t.id = "toast";
    t.className = "toast";
    document.body.appendChild(t);
  }
  t.textContent = msg;
  t.classList.add("show");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => t.classList.remove("show"), 2500);
}

function debounce(fn, ms) {
  let t;
  return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); };
}

function escapeHtml(s) {
  return String(s == null ? "" : s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}
