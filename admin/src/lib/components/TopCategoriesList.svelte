<script lang="ts">
	import type { CategoryCountDto } from '@pcecom/shared';

	let { data }: { data: CategoryCountDto[] } = $props();

	const max = $derived(Math.max(1, ...data.map((d) => d.count)));
</script>

<div class="rounded-2xl border border-slate-200 bg-white p-6">
	<h2 class="mb-4 text-base font-bold">Catégories les plus fournies</h2>
	{#if data.length === 0}
		<p class="py-4 text-center text-sm text-slate-400">Pas encore de données.</p>
	{:else}
		<div class="space-y-3">
			{#each data as c (c.categoryId)}
				<div>
					<div class="mb-1 flex justify-between text-sm">
						<span class="font-semibold text-slate-700">{c.name}</span>
						<span class="text-slate-500">{c.count}</span>
					</div>
					<div class="h-2 overflow-hidden rounded-full bg-slate-100">
						<div class="h-full rounded-full bg-teal-600" style="width: {(c.count / max) * 100}%"></div>
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>
