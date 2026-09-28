<script lang="ts">
	import { onMount } from 'svelte';
	import { api } from '$lib/api';
	import { money, type StatsDto } from '@pcecom/shared';
	import { catalog, loadAll } from '$lib/state/catalog.svelte';
	import StatCard from '$lib/components/StatCard.svelte';
	import ProductTable from '$lib/components/ProductTable.svelte';

	let stats = $state<StatsDto | null>(null);

	async function refresh() {
		await loadAll();
		stats = await api.request<StatsDto>('/admin/stats');
	}

	onMount(refresh);
</script>

<div class="mb-6">
	<h1 class="m-0 text-2xl font-extrabold">Tableau de bord</h1>
	<p class="mt-1 text-sm text-slate-500">Vue d'ensemble de votre boutique.</p>
</div>

{#if stats}
	<div class="mb-6 grid gap-3.5" style="grid-template-columns: repeat(auto-fit, minmax(160px, 1fr))">
		<StatCard label="Disponibles" value={stats.productsAvailable} />
		<StatCard label="Vendus ce mois" value={stats.soldThisMonth} />
		<StatCard label="Clics (30j)" value={stats.clicksLast30Days} />
		<StatCard label="Revenu ce mois" value={money(stats.revenueThisMonthTnd)} />
	</div>
{/if}

<ProductTable products={catalog.products} onChanged={refresh} />
