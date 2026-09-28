<script lang="ts">
	import { page } from '$app/state';
	import { money, specLine, priceInfo, waLink, CONDITION_LABEL } from '@pcecom/shared';
	import { conditionInfo } from '$lib/badges';
	import WhatsAppIcon from '$lib/components/WhatsAppIcon.svelte';

	let { data } = $props();
	const product = $derived(data.product);
	const photos = $derived(product.photos ?? []);
	const condition = $derived(conditionInfo(product));
	const info = $derived(priceInfo(product));

	let photoIndex = $state(0);
	const currentPhoto = $derived(photos[photoIndex] ?? null);

	function prevPhoto() {
		photoIndex = (photoIndex - 1 + photos.length) % (photos.length || 1);
	}
	function nextPhoto() {
		photoIndex = (photoIndex + 1) % (photos.length || 1);
	}

	const specRows = $derived(
		(
			[
				['Processeur', product.processor],
				['Mémoire RAM', product.ramGb ? `${product.ramGb} Go` : null],
				['Stockage', product.storageGb ? `${product.storageGb} Go · ${product.storageType || ''}` : null],
				['Carte graphique', product.gpu],
				['Écran', product.screenInches ? `${product.screenInches} pouces` : null],
				['Système', product.os],
				['Clavier', product.keyboard],
				['Garantie', product.warrantyMonths ? `${product.warrantyMonths} mois (boutique)` : null]
			] as [string, string | null][]
		).filter(([, v]) => v)
	);

	const productUrl = $derived(`${page.url.origin}/produits/${product.slug}`);
	const waHref = $derived(waLink(data.store, { ...product, productUrl }));
	const waPreview = $derived(decodeURIComponent(new URL(waHref).searchParams.get('text') ?? ''));
</script>

<svelte:head>
	<title>{product.brand} {product.model} — Meilleurs Prix Ordinateur</title>
	<meta name="description" content={`${product.brand} ${product.model} — ${specLine(product)} — ${money(product.priceTnd)}.`} />
</svelte:head>

<nav class="breadcrumb">
	<a href="/">Accueil</a> <span>›</span> <a href="/produits">Ordinateurs</a>
	{#if data.breadcrumbPath}
		{#each data.breadcrumbPath as c (c.id)}
			<span>›</span> <a href={`/produits?category=${encodeURIComponent(c.slug)}`}>{c.name}</a>
		{/each}
	{/if}
</nav>

<div class="pdp-shell">
	<div>
		<div class="gallery-main">
			{#if currentPhoto}
				<img src={currentPhoto.large} alt={`${product.brand} ${product.model}`} />
			{:else}
				photo 1 · aucune image
			{/if}
			{#if photos.length > 1}
				<button class="gallery-nav prev" onclick={prevPhoto} aria-label="Photo précédente">‹</button>
				<button class="gallery-nav next" onclick={nextPhoto} aria-label="Photo suivante">›</button>
				<span class="gallery-count">{photoIndex + 1} / {photos.length}</span>
			{/if}
		</div>
		{#if photos.length}
			<div class="thumb-strip">
				{#each photos as p, i (p.id)}
					<button class="thumb" class:active={i === photoIndex} onclick={() => (photoIndex = i)}>
						<img src={p.thumb} alt="" />
					</button>
				{/each}
			</div>
		{/if}
	</div>

	<div>
		<div class="pdp-badges">
			{#if !product.isUsed}
				<span class="badge neuf">Neuf</span>
			{:else}
				<span class="badge occ"><span class="dot {condition.dotClass}"></span>Occasion · {condition.label}</span>
			{/if}
			<span class="ref">Réf. #{product.id}</span>
		</div>
		<div class="pdp-brand">{product.brand}</div>
		<h1 class="pdp-title">{product.brand} {product.model}</h1>
		<div class="pdp-specline">{specLine(product)}</div>
		<div class="pdp-price">
			<span>{money(info.price)}</span>
			{#if info.compareAt}
				<span class="price-old">{money(info.compareAt)}</span>
				<span class="discount-badge">-{info.discountPct}%</span>
			{/if}
			{#if product.status === 'sold'}
				<span class="stock-ok" style="color:var(--muted-2)"><span class="dot" style="background:var(--muted-2)"></span>Vendu</span>
			{:else if product.quantity <= 1}
				<span class="stock-ok"><span class="dot"></span>En stock · dernier exemplaire</span>
			{:else}
				<span class="stock-ok"><span class="dot"></span>En stock · {product.quantity} disponibles</span>
			{/if}
		</div>

		{#if product.status !== 'sold'}
			<a class="wa-cta" href={waHref} target="_blank" rel="noopener">
				<WhatsAppIcon size={20} />Contacter sur WhatsApp
			</a>
			<div class="wa-preview">
				<div class="label">Message pré-rempli</div>
				<p>{waPreview}</p>
			</div>
		{/if}

		<div class="info-cards">
			<div class="info-card">
				<h4>Livraison partout en Tunisie</h4>
				<p>7 TND · 24 à 48 h · paiement à la livraison</p>
			</div>
			<div class="info-card">
				<h4>Retrait en boutique</h4>
				<p>{data.store.address || 'Tunis - LAFAYETTE'} · Lun–Sam 9h–19h</p>
			</div>
		</div>
	</div>
</div>

<div class="pdp-panels">
	<div class="panel">
		<h2>Caractéristiques techniques</h2>
		<table class="spec-table">
			<tbody>
				{#each specRows as [k, v] (k)}
					<tr><td>{k}</td><td>{v}</td></tr>
				{/each}
			</tbody>
		</table>
	</div>
	{#if product.isUsed && product.used}
		<div class="panel">
			<h2>État de l'appareil</h2>
			{#if product.used.batteryHealth != null}
				<div class="batt-row"><span>Santé de la batterie</span><strong>{product.used.batteryHealth} %</strong></div>
				<div class="batt-bar"><span style={`width:${product.used.batteryHealth}%`}></span></div>
			{/if}
			<div class="state-row"><span>État général</span><span>{CONDITION_LABEL[product.used.condition] || product.used.condition}</span></div>
			<div class="state-row"><span>Défauts constatés</span><span>{product.used.defects || 'Aucun'}</span></div>
			<div class="state-row"><span>Accessoires fournis</span><span>{product.used.accessories || '—'}</span></div>
		</div>
	{/if}
</div>

<div class="section" style="max-width:1180px;margin-left:auto;margin-right:auto">
	<div class="panel">
		<h2>Description</h2>
		<p class="desc-text">{product.description || 'Aucune description fournie.'}</p>
	</div>
</div>
