<script lang="ts">
	import type { Snippet } from 'svelte';

	let {
		title,
		onClose,
		children,
		maxWidth = '480px'
	}: { title: string; onClose: () => void; children: Snippet; maxWidth?: string } = $props();
</script>

<div
	class="fixed inset-0 z-[200] flex items-center justify-center bg-slate-900/50 p-5"
	onclick={(e) => {
		if (e.target === e.currentTarget) onClose();
	}}
>
	<div class="max-h-[86vh] w-full overflow-auto rounded-2xl bg-white p-6" style="max-width:{maxWidth}">
		<div class="mb-4 flex items-start justify-between gap-3">
			<h2 class="m-0 text-lg font-bold">{title}</h2>
			<button onclick={onClose} class="text-xl leading-none text-slate-400 hover:text-slate-700">&times;</button>
		</div>
		{@render children()}
	</div>
</div>
