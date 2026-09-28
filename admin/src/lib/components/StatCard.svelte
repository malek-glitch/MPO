<script lang="ts">
	let { label, value }: { label: string; value: string | number } = $props();
</script>

<div class="rounded-2xl border border-slate-200 bg-white p-4.5">
	<div class="text-2xl font-extrabold text-slate-900">{value}</div>
	<div class="mt-0.5 text-xs text-slate-500">{label}</div>
</div>
