<script lang="ts">
	import { FB_ICON_PATH } from '$lib/fbIcon';
	let { size = 16 }: { size?: number } = $props();
</script>

<svg class="fb-icon" viewBox="0 0 24 24" width={size} height={size} fill="currentColor" aria-hidden="true">
	<path d={FB_ICON_PATH} />
</svg>
