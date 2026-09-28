<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import ProductCard from '$lib/components/ProductCard.svelte';
	import CategorySidebar from '$lib/components/CategorySidebar.svelte';
	import { debounce } from '$lib/debounce';

	let { data } = $props();

	let searchInput = $state(data.filters.q);
	$effect(() => {
		searchInput = data.filters.q;
	});

	function currentParams() {
		return new URLSearchParams(page.url.searchParams);
	}

	function navigate(params: URLSearchParams, opts: { scrollTop?: boolean } = {}) {
		const qsStr = params.toString();
		goto(qsStr ? `/produits?${qsStr}` : '/produits', {
			replaceState: true,
			keepFocus: true,
			noScroll: !opts.scrollTop
		});
		if (opts.scrollTop) window.scrollTo(0, 0);
	}

	function setFilter(key: string, value: string, resetPage = true) {
		const params = currentParams();
		if (value) params.set(key, value);
		else params.delete(key);
		if (resetPage) params.delete('page');
		navigate(params);
	}

	function setRange(minKey: string, maxKey: string, value: string) {
		const params = currentParams();
		if (value) {
			const [min, max] = value.split('-');
			params.set(minKey, min);
			params.set(maxKey, max);
		} else {
			params.delete(minKey);
			params.delete(maxKey);
		}
		params.delete('page');
		navigate(params);
	}

	const debouncedSearch = debounce((value: string) => setFilter('q', value), 350);

	function goToPage(next: number) {
		const params = currentParams();
		if (next > 1) params.set('page', String(next));
		else params.delete('page');
		navigate(params, { scrollTop: true });
	}

	const screenValue = $derived(data.filters.screenMin ? `${data.filters.screenMin}-${data.filters.screenMax}` : '');
	const budgetValue = $derived(data.filters.priceMin ? `${data.filters.priceMin}-${data.filters.priceMax}` : '');
	const pages = $derived(Math.max(1, Math.ceil(data.total / data.pageSize)));
</script>

<svelte:head>
	<title>{data.title} — Meilleurs Prix Ordinateur</title>
</svelte:head>

<div class="list-shell">
	<CategorySidebar nodes={data.sidebar} activeSlug={data.filters.category} totalAll={data.totalAll} />

	<main class="list-main">
		<h1>{data.title}</h1>
		<div class="list-meta-row">
			<span class="list-count">{data.total} produit{data.total === 1 ? '' : 's'} au total</span>
			<div class="sort-row">
				Trier par
				<select value={data.filters.sort} onchange={(e) => setFilter('sort', e.currentTarget.value, false)}>
					<option value="newest">Plus récents</option>
					<option value="price_asc">Prix croissant</option>
					<option value="price_desc">Prix décroissant</option>
				</select>
			</div>
		</div>

		<div class="filter-bar">
			<div class="seg">
				<button type="button" class:active={!data.filters.used} onclick={() => setFilter('used', '')}>Tous</button>
				<button type="button" class:active={data.filters.used === 'false'} onclick={() => setFilter('used', 'false')}>Neuf</button>
				<button type="button" class:active={data.filters.used === 'true'} onclick={() => setFilter('used', 'true')}>Occasion</button>
			</div>
			<select value={data.filters.brand} onchange={(e) => setFilter('brand', e.currentTarget.value)}>
				<option value="">Marque</option>
				{#each data.brands as b (b)}<option value={b}>{b}</option>{/each}
			</select>
			<select value={data.filters.ram} onchange={(e) => setFilter('ram', e.currentTarget.value)}>
				<option value="">RAM</option>
				<option value="4">4 Go</option>
				<option value="8">8 Go</option>
				<option value="16">16 Go</option>
				<option value="32">32 Go</option>
				<option value="64">64 Go</option>
			</select>
			<select value={data.filters.storageMin} onchange={(e) => setFilter('storageMin', e.currentTarget.value)}>
				<option value="">Stockage</option>
				<option value="128">128 Go et +</option>
				<option value="256">256 Go et +</option>
				<option value="512">512 Go et +</option>
				<option value="1024">1 To et +</option>
			</select>
			<select value={screenValue} onchange={(e) => setRange('screenMin', 'screenMax', e.currentTarget.value)}>
				<option value="">Écran</option>
				<option value="0-13.9">13" et moins</option>
				<option value="14-15.9">14" – 15"</option>
				<option value="16-99">16" et plus</option>
			</select>
			<select value={budgetValue} onchange={(e) => setRange('priceMin', 'priceMax', e.currentTarget.value)}>
				<option value="">Budget</option>
				<option value="0-1000">Moins de 1 000 TND</option>
				<option value="1000-2000">1 000 – 2 000 TND</option>
				<option value="2000-3500">2 000 – 3 500 TND</option>
				<option value="3500-5000">3 500 – 5 000 TND</option>
				<option value="5000-999999">Plus de 5 000 TND</option>
			</select>
			<input
				type="text"
				placeholder="Rechercher une marque ou un modèle…"
				bind:value={searchInput}
				oninput={() => debouncedSearch(searchInput)}
			/>
		</div>

		{#if data.products.length}
			<div class="product-grid">
				{#each data.products as p (p.id)}
					<ProductCard product={p} />
				{/each}
			</div>
		{:else}
			<p class="empty">Aucun produit ne correspond à ces filtres.</p>
		{/if}

		{#if pages > 1}
			<div style="display:flex;justify-content:center;gap:10px;margin-top:28px">
				<button class="btn btn-outline-wa" disabled={data.filters.page <= 1} onclick={() => goToPage(data.filters.page - 1)}
					>← Précédent</button
				>
				<span style="align-self:center;font-size:13.5px;color:var(--muted)">Page {data.filters.page} / {pages}</span>
				<button class="btn btn-outline-wa" disabled={data.filters.page >= pages} onclick={() => goToPage(data.filters.page + 1)}
					>Suivant →</button
				>
			</div>
		{/if}
	</main>
</div>
