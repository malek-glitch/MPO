<script lang="ts">
	import { WA_ICON_PATH } from '$lib/waIcon';
	let { size = 18 }: { size?: number } = $props();
</script>

<svg class="wa-icon" viewBox="0 0 32 32" width={size} height={size} fill="currentColor" aria-hidden="true">
	<path d={WA_ICON_PATH} />
</svg>
