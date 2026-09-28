<script lang="ts">
	import { money, waLink, type ProductDto } from '@pcecom/shared';
	import { conditionInfo } from '$lib/badges';
	import ProductCard from '$lib/components/ProductCard.svelte';
	import WhatsAppIcon from '$lib/components/WhatsAppIcon.svelte';
	import Testimonials from '$lib/components/Testimonials.svelte';

	let { data } = $props();

	const generalWaLink = $derived(waLink(data.store, null));

	function heroTag(p: ProductDto | null) {
		if (!p) return null;
		return p.isUsed ? conditionInfo(p) : { label: 'Neuf', dotClass: '' };
	}
</script>

<svelte:head>
	<title>{data.store.name} — Vente &amp; achat PC &amp; Mac</title>
	<meta
		name="description"
		content="PC portables, MacBook et PC de bureau neufs ou d'occasion à {data.store.address ?? 'Tunis'}. Occasions testées en atelier et garanties."
	/>
</svelte:head>

<section class="hero">
	<div class="hero-copy">
		<span class="hero-badge"><span class="dot"></span>Espace de Vente et achat PC &amp; MAC</span>
		<h1>Le bon ordinateur,<br />au meilleur prix.</h1>
		<p class="lead">
			PC portables, MacBook et PC de bureau, neufs ou d'occasion. Chaque occasion est testée en atelier
			et garantie. Vous choisissez, vous nous écrivez sur WhatsApp, on vous livre. Nous rachetons
			aussi votre ancien PC ou Mac.
		</p>
		<div class="hero-pills">
			<span class="pill blue"><span class="dot"></span>20 ans d'expertise, 10 ans de service</span>
			<span class="pill pink"><span class="dot"></span>{data.store.address || 'Tunis - LAFAYETTE'}</span>
		</div>
		<div class="hero-ctas">
			<a class="btn btn-primary" href="/produits">Voir les ordinateurs</a>
			<a class="btn btn-outline-wa" href={generalWaLink} target="_blank" rel="noopener">
				<WhatsAppIcon />Écrire sur WhatsApp
			</a>
		</div>
	</div>
	<div class="hero-visual">
		<span class="hero-flag">Coup de cœur</span>
		<div class="hero-card">
			{#if data.hero}
				{@const tag = heroTag(data.hero)}
				<span class="hero-tag"><span class="dot"></span>{tag?.label}{data.hero.used?.batteryHealth != null ? ` · Batt. ${data.hero.used.batteryHealth} %` : ''}</span>
			{/if}
			<div class="ph">
				{#if data.hero?.photos?.[0]}
					<img src={data.hero.photos[0].medium} alt="" />
				{:else}
					photo produit vedette
				{/if}
			</div>
			<div style="padding:14px 16px">
				<div style="font-size:11px;letter-spacing:.05em;text-transform:uppercase;color:var(--muted);font-weight:700">
					{data.hero?.brand ?? ''}
				</div>
				<div style="font-weight:700;font-size:15px;margin:3px 0 8px">{data.hero?.model ?? ''}</div>
				<div style="display:flex;justify-content:space-between;align-items:baseline">
					<div style="font-weight:800;font-size:18px">{data.hero ? money(data.hero.priceTnd) : ''}</div>
				</div>
			</div>
		</div>
	</div>
</section>

<div class="trust-row">
	<div class="trust-card">
		<span class="ic" style="background:#dbeafe"></span>
		<div><h3>Occasions testées</h3><p>Contrôle complet en atelier avant la mise en vente</p></div>
	</div>
	<div class="trust-card">
		<span class="ic" style="background:#fbcfe8"></span>
		<div><h3>Garantie jusqu'à 12 mois</h3><p>3 mois sur l'occasion, garantie constructeur sur le neuf</p></div>
	</div>
	<div class="trust-card">
		<span class="ic" style="background:#bfdbfe"></span>
		<div><h3>Livraison 24 à 48 h</h3><p>Partout en Tunisie pour 7 TND</p></div>
	</div>
	<div class="trust-card">
		<span class="ic" style="background:#fbcfe8"></span>
		<div><h3>Paiement à la livraison</h3><p>Vous payez à la réception</p></div>
	</div>
</div>

<div class="section">
	<div class="section-head"><h2>Parcourir par catégorie</h2></div>
	<div class="cat-grid">
		{#each data.categories as c (c.id)}
			<a class="cat-tile {c.tint}" href={`/produits?category=${encodeURIComponent(c.slug)}`}>
				<div class="ph">{c.name.toLowerCase()}</div>
				<div class="row">
					<h3>{c.name}</h3>
					<span class="count">{c.total} produit{c.total === 1 ? '' : 's'}</span>
				</div>
			</a>
		{/each}
	</div>
</div>

{#if data.featured.length}
	<div class="section">
		<span class="section-eyebrow">Sélection boutique</span>
		<div class="section-head">
			<h2>Produits vedettes</h2>
		</div>
		<div class="product-grid">
			{#each data.featured as p (p.id)}
				<ProductCard product={p} />
			{/each}
		</div>
	</div>
{/if}

<div class="section">
	<div class="section-head">
		<h2>Nouveautés</h2>
		<a class="see-all" href="/produits?sort=newest">Tout voir →</a>
	</div>
	<div class="product-grid">
		{#each data.newest as p (p.id)}
			<ProductCard product={p} />
		{:else}
			<p class="empty">Aucun produit pour le moment.</p>
		{/each}
	</div>
</div>

<div class="section">
	<span class="section-eyebrow">Testés et garantis</span>
	<div class="section-head">
		<div>
			<h2>Bonnes affaires d'occasion</h2>
			<p style="margin:6px 0 0;color:var(--muted);font-size:14px">
				État, santé de la batterie et défauts indiqués sur chaque fiche.
			</p>
		</div>
		<a class="see-all" href="/produits?used=true">Toutes les occasions →</a>
	</div>
	<div class="product-grid">
		{#each data.used as p (p.id)}
			<ProductCard product={p} />
		{:else}
			<p class="empty">Aucun produit pour le moment.</p>
		{/each}
	</div>
</div>

<div class="section">
	<div class="section-head"><h2>Commander en 3 étapes</h2></div>
	<div class="steps-grid">
		<div class="step-card">
			<div class="step-num">1</div>
			<h3>Choisissez votre PC</h3>
			<p>Filtrez par marque, RAM, budget ou état. Prix, état et batterie sont affichés sur chaque fiche.</p>
		</div>
		<div class="step-card">
			<div class="step-num">2</div>
			<h3>Écrivez-nous sur WhatsApp</h3>
			<p>Le bouton ouvre WhatsApp avec un message déjà rédigé pour le produit choisi.</p>
		</div>
		<div class="step-card">
			<div class="step-num">3</div>
			<h3>Recevez-le chez vous</h3>
			<p>Livraison en 24 à 48 h partout en Tunisie, ou retrait en boutique.</p>
		</div>
	</div>
</div>

<Testimonials facebookUrl={data.store.facebookUrl} />

<div class="section" id="nous-trouver">
	<div class="section-head"><h2>Nous trouver</h2></div>
	<div class="find-grid">
		<div class="find-card">
			<div class="brand-row">
				<img src={data.store.logoUrl ?? '/assets/mpo-logo.png'} alt="" />
				<div>
					<div style="font-weight:700">{data.store.name}</div>
					<div style="font-size:12.5px;color:var(--muted)">Espace de Vente et achat PC &amp; MAC</div>
				</div>
			</div>
			<div class="loc"><span class="dot"></span>{data.store.address || 'Tunis - LAFAYETTE'}</div>
			<div class="meta">20 ans d'expertise, 10 ans de service<br />Lun–Sam 9h–19h</div>
			<div class="phone">{data.store.phone || data.store.whatsappNumber}</div>
			<div class="find-actions">
				<a class="btn-whatsapp" href={generalWaLink} target="_blank" rel="noopener">
					<WhatsAppIcon />WhatsApp
				</a>
				<a
					class="btn-outline"
					href="https://www.google.com/maps/search/?api=1&query=La+Fayette+Tunis"
					target="_blank"
					rel="noopener">Itinéraire →</a
				>
			</div>
		</div>
		<div class="map-frame">
			<iframe
				title="Localisation"
				loading="lazy"
				src="https://www.google.com/maps?q=La%20Fayette%2C%20Tunis%2C%20Tunisia&output=embed"
			></iframe>
		</div>
	</div>
</div>
