// Renders the shared footer into <footer id="footer"></footer>
(function () {
  async function renderFooter() {
    const el = document.getElementById("footer");
    if (!el) return;
    const store = await getStore();
    const phone = formatWa(store.whatsappNumber);
    el.innerHTML = `
      <div class="footer-top">
        <div class="footer-brand">
          <img data-store="logo" src="assets/mpo-logo.png" alt="">
          <div>
            <h3 data-store="name">${escapeHtml(store.name || "Meilleurs Prix Ordinateur")}</h3>
            <p>Espace de Vente et achat PC &amp; MAC.<br>20 ans d'expertise, 10 ans de service.</p>
          </div>
        </div>
        <div class="footer-col">
          <h4>Contact</h4>
          <ul>
            <li>WhatsApp : <span data-store="whatsapp-number">${escapeHtml(phone)}</span></li>
            <li>Téléphone : <span data-store="phone">${escapeHtml(store.phone || phone)}</span></li>
            <li><span data-store="address">${escapeHtml(store.address || "Tunis")}</span></li>
            <li>Lun–Sam 9h–19h</li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>Livraison</h4>
          <ul>
            <li>Partout en Tunisie · 7 TND</li>
            <li>24 à 48 h ouvrées</li>
            <li>Paiement à la livraison</li>
            <li>Retrait gratuit en boutique</li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>Suivez-nous</h4>
          <ul>
            <li><a data-social="facebook" href="${store.facebookUrl || "#"}" target="_blank" rel="noopener">Facebook</a></li>
            <li><a data-social="instagram" href="${store.instagramUrl || "#"}" target="_blank" rel="noopener">Instagram</a></li>
            <li><a data-social="tiktok" href="${store.tiktokUrl || "#"}" target="_blank" rel="noopener">TikTok</a></li>
            <li><a href="admin.html">Espace admin</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">© ${new Date().getFullYear()} ${escapeHtml(store.name || "Meilleurs Prix Ordinateur")}</div>`;
  }
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", renderFooter);
  } else {
    renderFooter();
  }
})();
