<script lang="ts">
	import type { DailyCountDto } from '@pcecom/shared';

	let { data }: { data: DailyCountDto[] } = $props();

	let hovered = $state<number | null>(null);

	const max = $derived(Math.max(1, ...data.map((d) => d.count)));
	const total = $derived(data.reduce((sum, d) => sum + d.count, 0));

	function formatDate(iso: string): string {
		const d = new Date(iso + 'T00:00:00');
		return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
	}
</script>

<div class="rounded-2xl border border-slate-200 bg-white p-6">
	<div class="mb-4 flex items-baseline justify-between">
		<h2 class="m-0 text-base font-bold">Ventes des 30 derniers jours</h2>
		<span class="text-sm text-slate-500">{total} vendu{total > 1 ? 's' : ''}</span>
	</div>
	{#if total === 0}
		<p class="py-8 text-center text-sm text-slate-400">Aucune vente sur cette période.</p>
	{:else}
		<div class="relative h-40">
			<svg viewBox="0 0 300 120" preserveAspectRatio="none" class="h-full w-full overflow-visible">
				<line x1="0" y1="119" x2="300" y2="119" stroke="#e2e8f0" stroke-width="1" />
				{#each data as d, i (d.date)}
					{@const barWidth = Math.max(300 / data.length - 2, 1)}
					{@const x = i * (300 / data.length)}
					{@const h = Math.max((d.count / max) * 110, d.count > 0 ? 3 : 0)}
					<rect
						x={x}
						y={118 - h}
						width={barWidth}
						height={h}
						rx="2"
						fill={hovered === i ? '#0f766e' : '#0d9488'}
						role="img"
						aria-label={`${formatDate(d.date)}: ${d.count}`}
						onmouseenter={() => (hovered = i)}
						onmouseleave={() => (hovered = null)}
					/>
				{/each}
			</svg>
			{#if hovered !== null}
				{@const d = data[hovered]}
				<div
					class="pointer-events-none absolute -top-1 rounded-md bg-slate-900 px-2 py-1 text-xs whitespace-nowrap text-white"
					style="left: {(hovered / data.length) * 100}%"
				>
					{formatDate(d.date)} · {d.count} vendu{d.count > 1 ? 's' : ''}
				</div>
			{/if}
		</div>
	{/if}
</div>
