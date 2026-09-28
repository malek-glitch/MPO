<script lang="ts">
	import { onMount } from 'svelte';
	import { api } from '$lib/api';
	import { money, type StatsDto } from '@pcecom/shared';
	import StatCard from '$lib/components/StatCard.svelte';
	import SalesChart from '$lib/components/SalesChart.svelte';
	import TopCategoriesList from '$lib/components/TopCategoriesList.svelte';

	let stats = $state<StatsDto | null>(null);

	onMount(async () => {
		stats = await api.request<StatsDto>('/admin/stats');
	});
</script>

<div class="mb-6">
	<h1 class="m-0 text-2xl font-extrabold">Statistiques</h1>
	<p class="mt-1 text-sm text-slate-500">Revenus, ventes et catégories les plus actives.</p>
</div>

{#if stats}
	<div class="mb-6 grid gap-3.5" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr))">
		<StatCard label="Revenu ce mois" value={money(stats.revenueThisMonthTnd)} />
		<StatCard label="Revenu total" value={money(stats.revenueAllTimeTnd)} />
		<StatCard label="Vendus ce mois" value={stats.soldThisMonth} />
		<StatCard label="Clics (30j)" value={stats.clicksLast30Days} />
	</div>
	<div class="mb-5 grid gap-5" style="grid-template-columns: 1.5fr 1fr">
		<SalesChart data={stats.salesLast30Days} />
		<TopCategoriesList data={stats.topCategories} />
	</div>
{/if}
