<script lang="ts">
	import { money, priceInfo, type ProductDto } from '@pcecom/shared';
	import { api } from '$lib/api';
	import { toast } from '$lib/state/toast.svelte';
	import ImageIcon from '@lucide/svelte/icons/image';
	import Tag from '@lucide/svelte/icons/tag';
	import Star from '@lucide/svelte/icons/star';
	import PhotoManagerModal from './PhotoManagerModal.svelte';
	import PromoFeaturedModal from './PromoFeaturedModal.svelte';

	let { products, onChanged }: { products: ProductDto[]; onChanged: () => void } = $props();

	const STATUS_LABEL: Record<string, string> = { available: 'Disponible', sold: 'Vendu', hidden: 'Masqué' };
	const STATUS_CLASS: Record<string, string> = {
		available: 'bg-green-100 text-green-700',
		sold: 'bg-red-100 text-red-700',
		hidden: 'bg-slate-100 text-slate-500'
	};

	let photoModalProduct = $state<ProductDto | null>(null);
	let promoModalProduct = $state<ProductDto | null>(null);

	async function markSold(p: ProductDto) {
		try {
			await api.request(`/admin/products/${p.id}/status`, {
				method: 'PATCH',
				body: JSON.stringify({ status: 'sold' })
			});
			toast.show('Marqué comme vendu');
			onChanged();
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}

	async function remove(p: ProductDto) {
		if (!confirm('Supprimer ce produit ?')) return;
		try {
			await api.request(`/admin/products/${p.id}`, { method: 'DELETE' });
			toast.show('Produit supprimé');
			onChanged();
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}
</script>

<div class="rounded-2xl border border-slate-200 bg-white p-6">
	<h2 class="mb-4.5 text-base font-bold">Produits</h2>
	<table class="w-full border-collapse text-sm">
		<thead>
			<tr class="text-[11px] font-bold tracking-wide text-slate-500 uppercase">
				<th class="py-3 text-left"></th>
				<th class="py-3 text-left">Marque / Modèle</th>
				<th class="py-3 text-left">Prix</th>
				<th class="py-3 text-left">Qté</th>
				<th class="py-3 text-left">Statut</th>
				<th class="py-3 text-left"></th>
			</tr>
		</thead>
		<tbody>
			{#each products as p (p.id)}
				{@const info = priceInfo(p)}
				<tr class="border-b border-slate-100 last:border-0">
					<td class="py-3">
						<div class="flex h-11 w-11 items-center justify-center overflow-hidden rounded-lg bg-slate-100 text-slate-400">
							{#if p.photos[0]}
								<img src={p.photos[0].thumb} alt="" class="h-full w-full object-cover" />
							{:else}
								<ImageIcon size={16} />
							{/if}
						</div>
					</td>
					<td class="py-3">
						{p.brand} {p.model}
						{#if p.featured}<Star size={12} class="ml-1 inline fill-amber-500 text-amber-500" />{/if}
					</td>
					<td class="py-3">
						{money(info.price)}
						{#if info.compareAt}<span class="block text-xs text-slate-400 line-through">{money(info.compareAt)}</span>{/if}
					</td>
					<td class="py-3">{p.quantity}</td>
					<td class="py-3">
						<span class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-bold {STATUS_CLASS[p.status]}">
							{STATUS_LABEL[p.status]}
						</span>
					</td>
					<td class="py-3">
						<div class="flex flex-wrap gap-1">
							<button
								onclick={() => (photoModalProduct = p)}
								class="rounded-md px-2 py-1 text-xs font-semibold text-teal-700 hover:bg-teal-50"
							>Photos{p.photos.length ? ` (${p.photos.length})` : ''}</button>
							<button
								onclick={() => (promoModalProduct = p)}
								class="flex items-center gap-1 rounded-md px-2 py-1 text-xs font-semibold text-teal-700 hover:bg-teal-50"
							><Tag size={12} />Promo</button>
							{#if p.status !== 'sold'}
								<button onclick={() => markSold(p)} class="rounded-md px-2 py-1 text-xs font-semibold text-teal-700 hover:bg-teal-50">
									Marquer vendu
								</button>
							{/if}
							<button onclick={() => remove(p)} class="rounded-md px-2 py-1 text-xs font-semibold text-red-600 hover:bg-red-50">
								Supprimer
							</button>
						</div>
					</td>
				</tr>
			{:else}
				<tr><td colspan="6" class="py-6 text-center text-slate-400">Aucun produit.</td></tr>
			{/each}
		</tbody>
	</table>
</div>

{#if photoModalProduct}
	<PhotoManagerModal
		product={photoModalProduct}
		onClose={() => (photoModalProduct = null)}
		onUpdated={() => onChanged()}
	/>
{/if}
{#if promoModalProduct}
	<PromoFeaturedModal
		product={promoModalProduct}
		onClose={() => (promoModalProduct = null)}
		onUpdated={() => onChanged()}
	/>
{/if}
