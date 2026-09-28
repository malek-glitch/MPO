<script lang="ts">
	import { goto } from '$app/navigation';
	import Sidebar from '$lib/components/Sidebar.svelte';
	import { auth, clearAuth } from '$lib/state/auth.svelte';
	import { api } from '$lib/api';
	import type { MeResponse } from '@pcecom/shared';

	let { children } = $props();
	let checking = $state(true);

	$effect(() => {
		if (!auth.token) {
			goto('/login');
			return;
		}
		if (auth.me) {
			checking = false;
			return;
		}
		api
			.request<MeResponse>('/admin/me')
			.then((me) => {
				auth.me = me;
				checking = false;
			})
			.catch(() => {
				clearAuth();
				goto('/login');
			});
	});
</script>

{#if auth.token}
	<div class="flex min-h-screen bg-slate-50">
		<Sidebar />
		<main class="max-w-5xl flex-1 p-8">
			{#if checking}
				<p class="text-sm text-slate-500">Chargement…</p>
			{:else}
				{@render children()}
			{/if}
		</main>
	</div>
{/if}
