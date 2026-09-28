<script lang="ts">
	import { page } from '$app/state';
	import type { StoreSettingsDto } from '@pcecom/shared';

	let { store }: { store: StoreSettingsDto } = $props();
	const isHome = $derived(page.url.pathname === '/');
	const isCatalog = $derived(page.url.pathname === '/produits');

	let catalogOpen = $state(false);
	let detailsEl: HTMLDetailsElement | undefined = $state();

	$effect(() => {
		if (!catalogOpen) return;
		function onDocClick(e: MouseEvent) {
			if (detailsEl && !detailsEl.contains(e.target as Node)) catalogOpen = false;
		}
		document.addEventListener('click', onDocClick);
		return () => document.removeEventListener('click', onDocClick);
	});
</script>

<header class="site-header">
	<div class="header-top">
		<a class="brand" href="/">
			<img class="logo" src={store.logoUrl ?? '/assets/mpo-logo.png'} alt="Logo" />
			<div>
				<div class="name">{store.name}</div>
				<div class="tagline">Vente &amp; achat PC &amp; Mac{store.address ? ` · ${store.address}` : ''}</div>
			</div>
		</a>
	</div>
	<nav class="site-nav">
		<a href="/" class:active={isHome}>Accueil</a>
		<details class="nav-dropdown" bind:this={detailsEl} bind:open={catalogOpen}>
			<summary class:active={isCatalog}>Catalogue <span class="chev">⌄</span></summary>
			<div class="nav-dropdown-panel">
				<a href="/produits?category=pc-portables" onclick={() => (catalogOpen = false)}>PC portables</a>
				<a href="/produits?category=macbook" onclick={() => (catalogOpen = false)}>MacBook</a>
				<a href="/produits?category=pc-de-bureau" onclick={() => (catalogOpen = false)}>PC de bureau</a>
				<a class="all" href="/produits" onclick={() => (catalogOpen = false)}>Tout le catalogue →</a>
			</div>
		</details>
		<a href="/produits?used=true">Occasions</a>
		<a href="/#nous-trouver">Nous trouver</a>
	</nav>
</header>
