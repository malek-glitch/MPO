<script lang="ts">
	import type { StoreSettingsDto } from '@pcecom/shared';

	let { store }: { store: StoreSettingsDto } = $props();

	function formatWa(n: string | null | undefined): string {
		if (!n) return '';
		const digits = String(n).replace(/\D/g, '');
		if (digits.startsWith('216') && digits.length === 11) {
			return '+216 ' + digits.slice(3, 5) + ' ' + digits.slice(5, 8) + ' ' + digits.slice(8);
		}
		return '+' + digits;
	}

	const phone = $derived(formatWa(store.whatsappNumber));
</script>

<footer class="site-footer">
	<div class="footer-top">
		<div class="footer-brand">
			<img src={store.logoUrl ?? '/assets/mpo-logo.png'} alt="" />
			<div>
				<h3>{store.name}</h3>
				<p>Espace de Vente et achat PC &amp; MAC.<br />20 ans d'expertise, 10 ans de service.</p>
			</div>
		</div>
		<div class="footer-col">
			<h4>Contact</h4>
			<ul>
				<li>WhatsApp : <span>{phone}</span></li>
				<li>Téléphone : <span>{store.phone || phone}</span></li>
				<li><span>{store.address || 'Tunis'}</span></li>
				<li>Lun–Sam 9h–19h</li>
			</ul>
		</div>
		<div class="footer-col">
			<h4>Livraison</h4>
			<ul>
				<li>{store.deliveryInfo || 'Partout en Tunisie · 7 TND'}</li>
				<li>24 à 48 h ouvrées</li>
				<li>Paiement à la livraison</li>
				<li>Retrait gratuit en boutique</li>
			</ul>
		</div>
		<div class="footer-col">
			<h4>Suivez-nous</h4>
			<ul>
				<li><a href={store.facebookUrl || '#'} target="_blank" rel="noopener">Facebook</a></li>
				<li><a href={store.instagramUrl || '#'} target="_blank" rel="noopener">Instagram</a></li>
				<li><a href={store.tiktokUrl || '#'} target="_blank" rel="noopener">TikTok</a></li>
			</ul>
		</div>
	</div>
	<div class="footer-bottom">© {new Date().getFullYear()} {store.name}</div>
</footer>
