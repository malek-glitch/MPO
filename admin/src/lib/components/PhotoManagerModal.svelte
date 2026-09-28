<script lang="ts">
	import Modal from './Modal.svelte';
	import { api } from '$lib/api';
	import { toast } from '$lib/state/toast.svelte';
	import type { ProductDto } from '@pcecom/shared';
	import Trash2 from '@lucide/svelte/icons/trash';

	let {
		product,
		onClose,
		onUpdated
	}: { product: ProductDto; onClose: () => void; onUpdated: (p: ProductDto) => void } = $props();

	let current = $state(product);
	let uploading = $state(false);

	async function addPhotos(files: FileList | null) {
		if (!files || !files.length) return;
		const form = new FormData();
		Array.from(files).forEach((f) => form.append('file', f));
		uploading = true;
		try {
			current = await api.request<ProductDto>(`/admin/products/${product.id}/photos`, {
				method: 'POST',
				body: form
			});
			onUpdated(current);
			toast.show(files.length > 1 ? 'Photos ajoutées' : 'Photo ajoutée');
		} catch (err) {
			toast.show("Échec de l'envoi : " + (err instanceof Error ? err.message : 'erreur'));
		} finally {
			uploading = false;
		}
	}

	async function deletePhoto(photoId: number) {
		try {
			current = await api.request<ProductDto>(`/admin/products/${product.id}/photos/${photoId}`, {
				method: 'DELETE'
			});
			onUpdated(current);
			toast.show('Photo supprimée');
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		}
	}
</script>

<Modal title={`Photos — ${product.brand} ${product.model}`} {onClose} maxWidth="560px">
	<p class="mb-4 text-sm text-slate-500">
		La première photo est utilisée comme photo principale. JPG/PNG/WebP, 12 photos max.
	</p>
	{#if current.photos.length}
		<div class="mb-4 grid grid-cols-4 gap-2.5">
			{#each current.photos as ph, i (ph.id)}
				<div
					class="relative aspect-square overflow-hidden rounded-lg border-2 bg-slate-100"
					class:border-teal-600={i === 0}
					class:border-transparent={i !== 0}
				>
					<img src={ph.medium} alt="" class="h-full w-full object-cover" />
					{#if i === 0}
						<span class="absolute bottom-1 left-1 rounded-full bg-teal-600 px-1.5 py-0.5 text-[9px] font-bold text-white">
							Principale
						</span>
					{/if}
					<button
						onclick={() => deletePhoto(ph.id)}
						class="absolute top-1 right-1 flex h-5 w-5 items-center justify-center rounded-full bg-slate-900/70 text-white"
						title="Supprimer"
					>
						<Trash2 size={11} />
					</button>
				</div>
			{/each}
		</div>
	{:else}
		<p class="mb-4 text-sm text-slate-500">Aucune photo pour ce produit pour le moment.</p>
	{/if}
	<label
		class="block cursor-pointer rounded-xl border-2 border-dashed border-slate-300 p-5.5 text-center text-sm text-slate-500 hover:border-teal-600 hover:bg-teal-50 hover:text-teal-700"
	>
		<input
			type="file"
			accept="image/*"
			multiple
			class="hidden"
			onchange={(e) => {
				const input = e.currentTarget;
				addPhotos(input.files);
				input.value = '';
			}}
		/>
		{uploading ? 'Envoi…' : '+ Ajouter des photos'}
	</label>
</Modal>
