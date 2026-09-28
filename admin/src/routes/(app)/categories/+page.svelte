<script lang="ts">
	import { onMount } from 'svelte';
	import { api } from '$lib/api';
	import { toast } from '$lib/state/toast.svelte';
	import type { CategoryDto, CategoryInput } from '@pcecom/shared';
	import Plus from '@lucide/svelte/icons/plus';
	import Pencil from '@lucide/svelte/icons/pencil';
	import Trash from '@lucide/svelte/icons/trash';
	import Eye from '@lucide/svelte/icons/eye';
	import EyeOff from '@lucide/svelte/icons/eye-off';
	import Check from '@lucide/svelte/icons/check';
	import X from '@lucide/svelte/icons/x';

	let categories = $state<CategoryDto[]>([]);
	let addingRootName = $state('');
	let addingChildFor = $state<number | null>(null);
	let addingChildName = $state('');
	let editingId = $state<number | null>(null);
	let editName = $state('');
	let editPosition = $state(0);

	async function refresh() {
		categories = await api.request<CategoryDto[]>('/admin/categories');
	}
	onMount(refresh);

	async function create(input: CategoryInput) {
		try {
			await api.request('/admin/categories', { method: 'POST', body: JSON.stringify(input) });
			toast.show('Catégorie créée');
			await refresh();
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}

	function addRoot() {
		if (!addingRootName.trim()) return;
		create({ name: addingRootName.trim() });
		addingRootName = '';
	}

	function addChild(parentId: number) {
		if (!addingChildName.trim()) return;
		create({ name: addingChildName.trim(), parentId });
		addingChildName = '';
		addingChildFor = null;
	}

	function startEdit(c: CategoryDto) {
		editingId = c.id;
		editName = c.name;
		editPosition = c.position;
	}

	async function saveEdit(c: CategoryDto) {
		try {
			const input: CategoryInput = { name: editName.trim(), parentId: c.parentId, position: editPosition, visible: c.visible };
			await api.request(`/admin/categories/${c.id}`, { method: 'PUT', body: JSON.stringify(input) });
			editingId = null;
			await refresh();
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}

	async function toggleVisible(c: CategoryDto) {
		try {
			const input: CategoryInput = { name: c.name, parentId: c.parentId, position: c.position, visible: !c.visible };
			await api.request(`/admin/categories/${c.id}`, { method: 'PUT', body: JSON.stringify(input) });
			await refresh();
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}

	async function remove(c: CategoryDto) {
		if (!confirm(`Supprimer "${c.name}" ?`)) return;
		try {
			await api.request(`/admin/categories/${c.id}`, { method: 'DELETE' });
			toast.show('Catégorie supprimée');
			await refresh();
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}
</script>

<div class="mb-6">
	<h1 class="m-0 text-2xl font-extrabold">Catégories</h1>
	<p class="mt-1 text-sm text-slate-500">Organisez le catalogue en catégories et sous-catégories.</p>
</div>

<div class="space-y-5 rounded-2xl border border-slate-200 bg-white p-6">
	{#each categories as cat (cat.id)}
		<div>
			<div class="flex items-center gap-2 py-1.5">
				{#if editingId === cat.id}
					<input bind:value={editName} class="flex-1 rounded-md border border-slate-300 px-2 py-1 text-sm" />
					<input type="number" bind:value={editPosition} class="w-16 rounded-md border border-slate-300 px-2 py-1 text-sm" />
					<button onclick={() => saveEdit(cat)} class="text-teal-700"><Check size={16} /></button>
					<button onclick={() => (editingId = null)} class="text-slate-400"><X size={16} /></button>
				{:else}
					<span class="flex-1 text-sm font-bold">{cat.name} <span class="font-normal text-slate-400">/{cat.slug}</span></span>
					<button onclick={() => toggleVisible(cat)} class="text-slate-400 hover:text-teal-700" title={cat.visible ? 'Masquer' : 'Afficher'}>
						{#if cat.visible}<Eye size={15} />{:else}<EyeOff size={15} />{/if}
					</button>
					<button onclick={() => startEdit(cat)} class="text-slate-400 hover:text-teal-700"><Pencil size={15} /></button>
					<button onclick={() => remove(cat)} class="text-slate-400 hover:text-red-600"><Trash size={15} /></button>
				{/if}
			</div>
			<div class="ml-5 space-y-1 border-l border-slate-200 pl-3">
				{#each cat.children as child (child.id)}
					<div class="flex items-center gap-2 py-1">
						{#if editingId === child.id}
							<input bind:value={editName} class="flex-1 rounded-md border border-slate-300 px-2 py-1 text-sm" />
							<input type="number" bind:value={editPosition} class="w-16 rounded-md border border-slate-300 px-2 py-1 text-sm" />
							<button onclick={() => saveEdit(child)} class="text-teal-700"><Check size={14} /></button>
							<button onclick={() => (editingId = null)} class="text-slate-400"><X size={14} /></button>
						{:else}
							<span class="flex-1 text-sm">{child.name} <span class="text-slate-400">/{child.slug}</span></span>
							<button onclick={() => toggleVisible(child)} class="text-slate-400 hover:text-teal-700">
								{#if child.visible}<Eye size={13} />{:else}<EyeOff size={13} />{/if}
							</button>
							<button onclick={() => startEdit(child)} class="text-slate-400 hover:text-teal-700"><Pencil size={13} /></button>
							<button onclick={() => remove(child)} class="text-slate-400 hover:text-red-600"><Trash size={13} /></button>
						{/if}
					</div>
				{/each}
				{#if addingChildFor === cat.id}
					<div class="flex items-center gap-2 py-1">
						<input
							bind:value={addingChildName}
							placeholder="Nom de la sous-catégorie"
							class="flex-1 rounded-md border border-slate-300 px-2 py-1 text-sm"
						/>
						<button onclick={() => addChild(cat.id)} class="text-xs font-bold text-teal-700">Ajouter</button>
						<button onclick={() => (addingChildFor = null)} class="text-xs text-slate-400">Annuler</button>
					</div>
				{:else}
					<button onclick={() => (addingChildFor = cat.id)} class="flex items-center gap-1 py-1 text-xs font-semibold text-teal-700">
						<Plus size={12} /> Sous-catégorie
					</button>
				{/if}
			</div>
		</div>
	{/each}

	<div class="flex items-center gap-2 border-t border-slate-100 pt-4">
		<input
			bind:value={addingRootName}
			placeholder="Nouvelle catégorie"
			class="flex-1 rounded-md border border-slate-300 px-2 py-1.5 text-sm"
		/>
		<button onclick={addRoot} class="flex items-center gap-1 rounded-md bg-teal-600 px-3 py-1.5 text-xs font-bold text-white hover:bg-teal-700">
			<Plus size={13} /> Ajouter
		</button>
	</div>
</div>
