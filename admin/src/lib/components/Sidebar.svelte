<script lang="ts">
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { clearAuth } from '$lib/state/auth.svelte';
	import LayoutDashboard from '@lucide/svelte/icons/layout-dashboard';
	import Plus from '@lucide/svelte/icons/plus';
	import FolderTree from '@lucide/svelte/icons/folder-tree';
	import ChartColumn from '@lucide/svelte/icons/chart-column';
	import Settings from '@lucide/svelte/icons/settings';
	import LogOut from '@lucide/svelte/icons/log-out';

	const nav = [
		{ href: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard },
		{ href: '/products/new', label: 'Ajouter un produit', icon: Plus },
		{ href: '/categories', label: 'Catégories', icon: FolderTree },
		{ href: '/analytics', label: 'Statistiques', icon: ChartColumn },
		{ href: '/settings', label: 'Paramètres', icon: Settings }
	];

	function logout() {
		clearAuth();
		goto('/login');
	}
</script>

<aside class="sticky top-0 flex h-screen w-60 shrink-0 flex-col border-r border-slate-200 bg-white p-3.5">
	<div class="flex items-center gap-2.5 px-2 pt-1.5 pb-5">
		<div class="flex h-9 w-9 items-center justify-center rounded-[10px] bg-teal-600 text-sm font-bold text-white">PC</div>
		<div class="text-sm leading-tight font-bold">Back office</div>
	</div>
	<nav class="flex flex-1 flex-col gap-0.5">
		{#each nav as item (item.href)}
			{@const active = page.url.pathname.startsWith(item.href)}
			<a
				href={item.href}
				class="flex items-center gap-2.5 rounded-lg px-3 py-2.5 text-sm font-semibold"
				class:bg-teal-50={active}
				class:text-teal-700={active}
				class:text-slate-500={!active}
			>
				<item.icon size={17} />
				{item.label}
			</a>
		{/each}
	</nav>
	<button
		onclick={logout}
		class="flex items-center gap-2.5 rounded-lg border border-slate-200 px-3 py-2.5 text-sm font-semibold text-slate-500 hover:border-red-400 hover:text-red-600"
	>
		<LogOut size={16} /> Déconnexion
	</button>
</aside>
