<script lang="ts">
	import Modal from './Modal.svelte';
	import { api } from '$lib/api';
	import { toast } from '$lib/state/toast.svelte';
	import { productInputFrom, type ProductDto } from '@pcecom/shared';

	let {
		product,
		onClose,
		onUpdated
	}: { product: ProductDto; onClose: () => void; onUpdated: (p: ProductDto) => void } = $props();

	let compareAt = $state(product.compareAtPriceTnd != null ? String(product.compareAtPriceTnd) : '');
	let featured = $state(product.featured);
	let error = $state('');
	let saving = $state(false);

	async function save() {
		error = '';
		saving = true;
		try {
			const input = productInputFrom(product);
			input.compareAtPriceTnd = compareAt.trim() ? Number(compareAt) : null;
			input.featured = featured;
			const updated = await api.request<ProductDto>(`/admin/products/${product.id}`, {
				method: 'PUT',
				body: JSON.stringify(input)
			});
			onUpdated(updated);
			toast.show('Produit mis à jour');
			onClose();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Erreur';
		} finally {
			saving = false;
		}
	}
</script>

<Modal title={`Promo & mise en avant — ${product.brand} ${product.model}`} {onClose}>
	<div class="space-y-4">
		<label class="block">
			<span class="mb-1.5 block text-sm font-semibold text-slate-700">Prix barré / avant promo (TND)</span>
			<input
				type="number"
				min="0"
				bind:value={compareAt}
				placeholder="Laisser vide si pas de promotion"
				class="w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm"
			/>
		</label>
		<label class="flex items-center gap-2.5 text-sm font-semibold text-slate-700">
			<input type="checkbox" bind:checked={featured} class="h-4 w-4 accent-teal-600" />
			Mettre en avant sur la page d'accueil
		</label>
		{#if error}<p class="text-sm text-red-600">{error}</p>{/if}
		<div class="flex gap-3 pt-2">
			<button
				onclick={save}
				disabled={saving}
				class="rounded-lg bg-teal-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-teal-700 disabled:opacity-60"
			>{saving ? 'Enregistrement…' : 'Enregistrer'}</button>
			<button onclick={onClose} class="px-3 text-sm font-semibold text-slate-500">Annuler</button>
		</div>
	</div>
</Modal>
