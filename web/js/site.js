// Populates the shared header/footer (store branding, WhatsApp links, active nav, social links)
let _storeCache = null;

async function getStore() {
  if (_storeCache) return _storeCache;
  _storeCache = await api("/store").catch(() => ({
    name: "Meilleurs Prix Ordinateur",
    logoUrl: null,
    whatsappNumber: "",
    whatsappTemplate: null,
  }));
  return _storeCache;
}

async function initSite(activeKey) {
  const store = await getStore();

  document.querySelectorAll('[data-store="name"]').forEach((el) => (el.textContent = store.name || "Meilleurs Prix Ordinateur"));
  document.querySelectorAll('[data-store="tagline"]').forEach((el) => {
    el.textContent = "Vente & achat PC & Mac" + (store.address ? " · " + store.address : "");
  });
  document.querySelectorAll('[data-store="phone"]').forEach((el) => (el.textContent = store.phone || formatWa(store.whatsappNumber)));
  document.querySelectorAll('[data-store="whatsapp-number"]').forEach((el) => (el.textContent = formatWa(store.whatsappNumber)));
  document.querySelectorAll('[data-store="address"]').forEach((el) => (el.textContent = store.address || "Tunis"));

  document.querySelectorAll('[data-store="logo"]').forEach((el) => {
    if (store.logoUrl) el.src = store.logoUrl;
  });

  document.querySelectorAll(".js-wa-link").forEach((el) => (el.href = waLink(store, null)));

  if (store.facebookUrl) document.querySelectorAll('[data-social="facebook"]').forEach((el) => (el.href = store.facebookUrl));
  if (store.instagramUrl) document.querySelectorAll('[data-social="instagram"]').forEach((el) => (el.href = store.instagramUrl));
  if (store.tiktokUrl) document.querySelectorAll('[data-social="tiktok"]').forEach((el) => (el.href = store.tiktokUrl));

  if (activeKey) {
    document.querySelectorAll("nav.site-nav a[data-nav]").forEach((a) => {
      a.classList.toggle("active", a.dataset.nav === activeKey);
    });
  }

  return store;
}

function formatWa(n) {
  if (!n) return "";
  const digits = String(n).replace(/\D/g, "");
  if (digits.startsWith("216") && digits.length === 11) {
    return "+216 " + digits.slice(3, 5) + " " + digits.slice(5, 8) + " " + digits.slice(8);
  }
  return "+" + digits;
}
