<script lang="ts">
	let {
		values,
		active,
		format = (v: string | number) => String(v),
		onSelect
	}: {
		values: (string | number)[];
		active: string;
		format?: (v: string | number) => string;
		onSelect: (v: string | number) => void;
	} = $props();
</script>

<div class="flex flex-wrap gap-1.5">
	{#each values as v (v)}
		{@const isActive = active.trim().toLowerCase() === String(v).toLowerCase()}
		<button
			type="button"
			onclick={() => onSelect(v)}
			class="rounded-full border px-3 py-1.5 text-sm font-semibold"
			class:bg-teal-600={isActive}
			class:border-teal-600={isActive}
			class:text-white={isActive}
			class:border-slate-300={!isActive}
			class:text-slate-700={!isActive}
		>{format(v)}</button>
	{/each}
</div>
