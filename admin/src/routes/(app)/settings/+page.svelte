<script lang="ts">
	import { onMount } from 'svelte';
	import { api } from '$lib/api';
	import { toast } from '$lib/state/toast.svelte';
	import type { StoreSettingsDto, StoreSettingsInput } from '@pcecom/shared';

	let settings = $state<StoreSettingsDto | null>(null);
	let form = $state<StoreSettingsInput>({ name: '', whatsappNumber: '' });
	let saving = $state(false);
	let uploadingLogo = $state(false);

	async function refresh() {
		const s = await api.request<StoreSettingsDto>('/admin/settings');
		settings = s;
		form = {
			name: s.name,
			whatsappNumber: s.whatsappNumber,
			phone: s.phone ?? '',
			address: s.address ?? '',
			facebookUrl: s.facebookUrl ?? '',
			instagramUrl: s.instagramUrl ?? '',
			tiktokUrl: s.tiktokUrl ?? '',
			whatsappTemplate: s.whatsappTemplate ?? '',
			deliveryInfo: s.deliveryInfo ?? ''
		};
	}
	onMount(refresh);

	async function save(e: SubmitEvent) {
		e.preventDefault();
		saving = true;
		try {
			settings = await api.request<StoreSettingsDto>('/admin/settings', {
				method: 'PUT',
				body: JSON.stringify(form)
			});
			toast.show('Paramètres enregistrés');
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		} finally {
			saving = false;
		}
	}

	async function uploadLogo(files: FileList | null) {
		if (!files || !files[0]) return;
		const body = new FormData();
		body.append('file', files[0]);
		uploadingLogo = true;
		try {
			settings = await api.request<StoreSettingsDto>('/admin/settings/logo', { method: 'POST', body });
			toast.show('Logo mis à jour');
		} catch (err) {
			toast.show(err instanceof Error ? err.message : 'Erreur');
		} finally {
			uploadingLogo = false;
		}
	}
</script>

<div class="mb-6">
	<h1 class="m-0 text-2xl font-extrabold">Paramètres</h1>
	<p class="mt-1 text-sm text-slate-500">Informations de votre boutique, visibles par vos clients.</p>
</div>

{#if settings}
	<div class="mb-5 flex items-center gap-4 rounded-2xl border border-slate-200 bg-white p-6">
		<div class="flex h-16 w-16 items-center justify-center overflow-hidden rounded-xl bg-slate-100">
			{#if settings.logoUrl}
				<img src={settings.logoUrl} alt="" class="h-full w-full object-cover" />
			{:else}
				<span class="text-xs text-slate-400">Logo</span>
			{/if}
		</div>
		<label class="cursor-pointer text-sm font-semibold text-teal-700">
			{uploadingLogo ? 'Envoi…' : 'Changer le logo'}
			<input
				type="file"
				accept="image/*"
				class="hidden"
				onchange={(e) => uploadLogo(e.currentTarget.files)}
			/>
		</label>
	</div>

	<form onsubmit={save} class="grid grid-cols-2 gap-4 rounded-2xl border border-slate-200 bg-white p-6">
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Nom de la boutique
			<input bind:value={form.name} required class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Numéro WhatsApp
			<input bind:value={form.whatsappNumber} required placeholder="21620123456" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Téléphone
			<input bind:value={form.phone} class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Adresse
			<input bind:value={form.address} class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Facebook
			<input bind:value={form.facebookUrl} class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Instagram
			<input bind:value={form.instagramUrl} class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">TikTok
			<input bind:value={form.tiktokUrl} class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Livraison
			<input bind:value={form.deliveryInfo} class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal" />
		</label>
		<label class="col-span-2 flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Message WhatsApp
			<textarea bind:value={form.whatsappTemplate} rows="3" class="resize-y rounded-lg border border-slate-300 px-3 py-2 text-sm font-normal"
			></textarea>
			<span class="text-xs font-normal text-slate-400">
				Utilisez {'{product}'}, {'{price}'} et {'{link}'} — remplacés automatiquement.
			</span>
		</label>
		<div class="col-span-2">
			<button
				type="submit"
				disabled={saving}
				class="rounded-lg bg-teal-600 px-6 py-2.5 text-sm font-bold text-white hover:bg-teal-700 disabled:opacity-60"
			>{saving ? 'Enregistrement…' : 'Enregistrer'}</button>
		</div>
	</form>
{/if}
