<script lang="ts">
	import { toast } from '$lib/state/toast.svelte';
</script>

{#if toast.message}
	<div class="fixed bottom-5 left-1/2 z-50 -translate-x-1/2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm text-white shadow-lg">
		{toast.message}
	</div>
{/if}
