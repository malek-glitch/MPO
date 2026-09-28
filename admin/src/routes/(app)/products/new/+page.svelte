<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { api } from '$lib/api';
	import { toast } from '$lib/state/toast.svelte';
	import { catalog, loadAll } from '$lib/state/catalog.svelte';
	import { CONDITION_LABEL, type ProductDto, type ProductInput, type UsedDetailsDto } from '@pcecom/shared';
	import Chips from '$lib/components/Chips.svelte';
	import PhotoDropzone from '$lib/components/PhotoDropzone.svelte';
	import {
		APPLE_SPECS,
		PC_SPECS,
		CHROMEOS_BRANDS,
		GAMING_BRANDS,
		COMMON_BRANDS,
		MORE_BRANDS,
		STORAGE_VALUES,
		WARRANTY_VALUES,
		OS_OTHER
	} from '$lib/constants/specs';

	type Condition = 'like_new' | 'very_good' | 'good' | 'fair';

	let isUsed = $state(false);
	let brand = $state('');
	let model = $state('');
	let categoryId = $state<number | ''>('');
	let categoryTouched = $state(false);
	let processor = $state('');
	let ramGb = $state('');
	let storageGb = $state('');
	let storageType = $state('SSD NVMe');
	let gpu = $state('');
	let screenInches = $state('');
	let osValue = $state('');
	let osOther = $state('');
	let keyboard = $state('AZERTY (FR)');
	let condition = $state<Condition>('like_new');
	let batteryHealth = $state('');
	let defects = $state('');
	let accessories = $state('');
	let priceTnd = $state('');
	let compareAtPriceTnd = $state('');
	let quantity = $state('1');
	let warrantyMonths = $state('12');
	let warrantyTouched = $state(false);
	let status = $state<'available' | 'hidden' | 'sold'>('available');
	let description = $state('');
	let photos = $state<{ file: File; url: string }[]>([]);
	let extraBrands = $state<string[]>([]);
	let prefillId = $state('');
	let error = $state('');
	let saving = $state(false);

	let formEl: HTMLFormElement | undefined;
	let saveButton: HTMLButtonElement | undefined;

	const isApple = $derived(brand.trim().toLowerCase() === 'apple');
	const specs = $derived(isApple ? APPLE_SPECS : PC_SPECS);
	const knownBrands = $derived.by(() => {
		const all = [...COMMON_BRANDS, ...MORE_BRANDS, ...extraBrands, ...catalog.products.map((p) => p.brand)];
		const seen = new Map<string, string>();
		all.forEach((b) => {
			if (b && !seen.has(b.toLowerCase())) seen.set(b.toLowerCase(), b);
		});
		return [...seen.values()];
	});
	const osList = $derived.by(() => {
		const list = [...specs.os];
		if (CHROMEOS_BRANDS.includes(brand.trim().toLowerCase())) list.push('ChromeOS');
		return list;
	});
	const modelSuggestions = $derived([
		...new Set(catalog.products.filter((p) => p.brand.toLowerCase() === brand.trim().toLowerCase()).map((p) => p.model))
	]);

	function categoryIdBySlug(slug: string): number | null {
		const c = catalog.flatCategories.find((x) => x.slug === slug);
		return c ? c.id : null;
	}

	function suggestCategory() {
		if (categoryTouched) return;
		const b = brand.trim().toLowerCase();
		const m = model.toLowerCase();
		const g = gpu.toLowerCase();
		let slug: string | undefined;
		if (b === 'apple') slug = m.includes('air') ? 'macbook-air' : m.includes('pro') ? 'macbook-pro' : 'macbook';
		else if (GAMING_BRANDS.includes(b) || g.includes('rtx')) slug = 'pc-portables-gaming';
		else if (b) slug = 'pc-portables';
		const id = slug && categoryIdBySlug(slug);
		if (id) categoryId = id;
	}

	function onBrandChange() {
		const typed = brand.trim();
		const known = knownBrands.find((b) => b.toLowerCase() === typed.toLowerCase());
		if (known) brand = known;
		if (isApple) storageType = 'SSD';
		else if (storageType === 'SSD') storageType = PC_SPECS.storageType;
		if (!osList.includes(osValue)) osValue = osList[0] ?? '';
		suggestCategory();
	}

	$effect(() => {
		void model;
		void gpu;
		suggestCategory();
	});

	$effect(() => {
		if (!warrantyTouched) warrantyMonths = isUsed ? '3' : '12';
		if (isUsed) quantity = '1';
	});

	onMount(async () => {
		await loadAll();
		try {
			extraBrands = await api.request<string[]>('/brands');
		} catch {
			/* optional */
		}
		if (!osValue) osValue = osList[0] ?? '';
	});

	function numOrNull(v: string): number | null {
		return v.trim() === '' ? null : Number(v);
	}
	function strOrNull(v: string): string | null {
		const t = v.trim();
		return t === '' ? null : t;
	}
	function storageLabel(gb: number): string {
		return gb >= 1024 ? `${gb / 1024} To` : `${gb} Go`;
	}

	function generateDescription() {
		if (!brand.trim() || !model.trim()) {
			toast.show("Renseignez d'abord la marque et le modèle");
			return;
		}
		const parts: string[] = [];
		if (processor.trim()) parts.push(processor.trim());
		if (ramGb) parts.push(`${ramGb} Go de RAM`);
		if (storageGb) parts.push(`${storageType} ${storageLabel(Number(storageGb))}`);
		if (gpu.trim()) parts.push(`carte graphique ${gpu.trim()}`);
		if (screenInches) parts.push(`écran ${String(screenInches).replace('.', ',')} pouces`);
		const os = osValue === OS_OTHER ? osOther.trim() : osValue;
		if (os) parts.push(os);

		let text = `${brand.trim()} ${model.trim()} ${isUsed ? `d'occasion, état ${CONDITION_LABEL[condition].toLowerCase()}` : 'neuf'}`;
		text += parts.length ? ` : ${parts.join(', ')}.` : '.';
		if (isUsed) {
			if (batteryHealth) text += ` Batterie à ${batteryHealth} %.`;
			text += defects.trim() && defects.trim().toLowerCase() !== 'aucun' ? ` Défauts : ${defects.trim()}.` : ' Aucun défaut constaté.';
			text += ' Testé en atelier.';
		}
		const w = Number(warrantyMonths || 0);
		if (w > 0) text += ` Garantie ${isUsed ? 'boutique ' : ''}${w} mois.`;
		description = text;
	}

	function applyPrefill() {
		const p = catalog.products.find((x) => String(x.id) === prefillId);
		if (!p) return;
		isUsed = p.isUsed;
		brand = p.brand;
		model = p.model;
		categoryId = p.categoryId;
		categoryTouched = true;
		processor = p.processor ?? '';
		ramGb = p.ramGb != null ? String(p.ramGb) : '';
		storageGb = p.storageGb != null ? String(p.storageGb) : '';
		if (p.storageType) storageType = p.storageType;
		gpu = p.gpu ?? '';
		screenInches = p.screenInches != null ? String(p.screenInches) : '';
		if (p.keyboard) keyboard = p.keyboard;
		if (!p.os) osValue = '';
		else if (osList.includes(p.os)) osValue = p.os;
		else {
			osValue = OS_OTHER;
			osOther = p.os;
		}
		if (p.used) {
			condition = p.used.condition;
			batteryHealth = p.used.batteryHealth != null ? String(p.used.batteryHealth) : '';
			defects = p.used.defects ?? '';
			accessories = p.used.accessories ?? '';
		}
		warrantyMonths = String(p.warrantyMonths);
		warrantyTouched = true;
		description = p.description ?? '';
		priceTnd = '';
		compareAtPriceTnd = '';
		toast.show('Caractéristiques copiées — indiquez le prix et ajoutez les photos');
	}

	function resetForm(keep: boolean) {
		const kept = keep ? { isUsed, brand, categoryId } : null;
		isUsed = false;
		brand = '';
		model = '';
		categoryId = '';
		processor = '';
		ramGb = '';
		storageGb = '';
		storageType = 'SSD NVMe';
		gpu = '';
		screenInches = '';
		osValue = '';
		osOther = '';
		keyboard = 'AZERTY (FR)';
		condition = 'like_new';
		batteryHealth = '';
		defects = '';
		accessories = '';
		priceTnd = '';
		compareAtPriceTnd = '';
		quantity = '1';
		warrantyMonths = '12';
		warrantyTouched = false;
		status = 'available';
		description = '';
		photos.forEach((p) => URL.revokeObjectURL(p.url));
		photos = [];
		categoryTouched = false;
		prefillId = '';
		error = '';
		if (kept) {
			isUsed = kept.isUsed;
			brand = kept.brand;
			categoryId = kept.categoryId;
			categoryTouched = true;
		}
	}

	async function submit(e: SubmitEvent) {
		e.preventDefault();
		error = '';
		const nextAction = (e.submitter as HTMLButtonElement | null)?.dataset.next ?? 'dashboard';

		const usedPayload: UsedDetailsDto | null = isUsed
			? {
					condition,
					batteryHealth: numOrNull(batteryHealth),
					defects: strOrNull(defects),
					accessories: strOrNull(accessories)
				}
			: null;
		const body: ProductInput = {
			categoryId: Number(categoryId),
			isUsed,
			brand: brand.trim(),
			model: model.trim(),
			processor: strOrNull(processor),
			ramGb: numOrNull(ramGb),
			storageGb: numOrNull(storageGb),
			storageType: strOrNull(storageType),
			gpu: strOrNull(gpu),
			screenInches: numOrNull(screenInches),
			os: (osValue === OS_OTHER ? osOther.trim() : osValue) || null,
			keyboard: strOrNull(keyboard),
			priceTnd: Number(priceTnd),
			compareAtPriceTnd: numOrNull(compareAtPriceTnd),
			quantity: isUsed ? 1 : Number(quantity || 1),
			status,
			description: strOrNull(description),
			warrantyMonths: Number(warrantyMonths || 0),
			used: usedPayload
		};

		saving = true;
		let created: ProductDto;
		try {
			created = await api.request<ProductDto>('/admin/products', { method: 'POST', body: JSON.stringify(body) });
		} catch (err) {
			error = err instanceof Error ? err.message : 'Erreur';
			saving = false;
			return;
		}

		if (photos.length) {
			const form = new FormData();
			photos.forEach((p) => form.append('file', p.file));
			try {
				created = await api.request<ProductDto>(`/admin/products/${created.id}/photos`, { method: 'POST', body: form });
			} catch (err) {
				toast.show('Produit enregistré, mais l’envoi des photos a échoué : ' + (err instanceof Error ? err.message : 'erreur'));
			}
		}
		saving = false;
		await loadAll();
		toast.show(
			`${created.brand} ${created.model} enregistré${created.photos.length ? ` avec ${created.photos.length} photo${created.photos.length > 1 ? 's' : ''}` : ' (sans photo)'}`
		);

		if (nextAction === 'another') {
			resetForm(true);
			window.scrollTo({ top: 0, behavior: 'smooth' });
		} else {
			goto('/dashboard');
		}
	}

	function onFormKeydown(e: KeyboardEvent) {
		if ((e.metaKey || e.ctrlKey) && e.key === 'Enter' && formEl && saveButton) {
			e.preventDefault();
			formEl.requestSubmit(saveButton);
		}
	}
</script>

<div class="mb-6">
	<h1 class="m-0 text-2xl font-extrabold">Ajouter un produit</h1>
	<p class="mt-1 text-sm text-slate-500">Renseignez les informations du nouvel article.</p>
</div>

<form bind:this={formEl} onsubmit={submit} onkeydown={onFormKeydown} class="space-y-5" autocomplete="off">
	<div class="flex flex-wrap items-center gap-3 rounded-2xl border border-teal-100 bg-teal-50 p-4">
		<label for="prefill" class="text-sm font-bold text-teal-800">Partir d'un produit existant</label>
		<select id="prefill" bind:value={prefillId} class="min-w-[240px] rounded-lg border border-teal-200 px-3 py-2 text-sm">
			<option value="">— Choisir un produit —</option>
			{#each catalog.products as p (p.id)}
				<option value={String(p.id)}>{p.brand} {p.model}{p.isUsed ? ' (occasion)' : ''}</option>
			{/each}
		</select>
		<button
			type="button"
			disabled={!prefillId}
			onclick={applyPrefill}
			class="rounded-lg border border-teal-600 px-3 py-1.5 text-sm font-bold text-teal-700 disabled:opacity-40"
		>Copier</button>
		<span class="text-xs text-teal-700/80">Copie toutes les caractéristiques ; il ne reste que le prix et les photos.</span>
	</div>

	<div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6">
		<h2 class="m-0 text-base font-bold">1. L'appareil</h2>
		<div class="flex gap-2.5">
			<label
				class="flex cursor-pointer items-center gap-1.5 rounded-lg border px-3.5 py-2 text-sm font-medium"
				class:border-teal-600={!isUsed}
				class:bg-teal-50={!isUsed}
				class:border-slate-300={isUsed}
			>
				<input type="radio" name="condType" checked={!isUsed} onchange={() => (isUsed = false)} class="accent-teal-600" /> Neuf
			</label>
			<label
				class="flex cursor-pointer items-center gap-1.5 rounded-lg border px-3.5 py-2 text-sm font-medium"
				class:border-teal-600={isUsed}
				class:bg-teal-50={isUsed}
				class:border-slate-300={!isUsed}
			>
				<input type="radio" name="condType" checked={isUsed} onchange={() => (isUsed = true)} class="accent-teal-600" /> Occasion
			</label>
		</div>
		<div>
			<span class="mb-1.5 block text-sm font-semibold text-slate-700">Marque</span>
			<Chips
				values={COMMON_BRANDS}
				active={brand}
				onSelect={(v) => {
					brand = String(v);
					onBrandChange();
				}}
			/>
			<input
				bind:value={brand}
				onchange={onBrandChange}
				required
				placeholder="Ou tapez une autre marque…"
				list="brandList"
				class="mt-2 w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm"
			/>
			<datalist id="brandList">{#each knownBrands as b (b)}<option value={b}></option>{/each}</datalist>
		</div>
		<div class="grid grid-cols-2 gap-4">
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Modèle
				<input bind:value={model} required placeholder="Latitude 7420" list="modelList" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
				<datalist id="modelList">{#each modelSuggestions as m (m)}<option value={m}></option>{/each}</datalist>
			</label>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Catégorie
				<select
					bind:value={categoryId}
					onchange={() => (categoryTouched = true)}
					required
					class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal"
				>
					<option value="" disabled>— Choisir —</option>
					{#each catalog.flatCategories as c (c.id)}<option value={c.id}>{c.label}</option>{/each}
				</select>
			</label>
		</div>
	</div>

	<div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6">
		<h2 class="m-0 text-base font-bold">2. Caractéristiques</h2>
		<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Processeur
			<input
				bind:value={processor}
				list="cpuList"
				placeholder={isApple ? 'Apple M2' : 'Intel Core i5-1145G7'}
				class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal"
			/>
			<datalist id="cpuList">{#each specs.cpu as c (c)}<option value={c}></option>{/each}</datalist>
		</label>
		<div class="grid grid-cols-2 gap-4">
			<div>
				<span class="mb-1.5 block text-sm font-semibold text-slate-700">Mémoire RAM (Go)</span>
				<Chips values={specs.ram} active={ramGb} format={(v) => `${v} Go`} onSelect={(v) => (ramGb = String(v))} />
				<input type="number" min="0" bind:value={ramGb} placeholder="Autre valeur" class="mt-2 w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm" />
			</div>
			<div>
				<span class="mb-1.5 block text-sm font-semibold text-slate-700">Stockage</span>
				<Chips values={STORAGE_VALUES} active={storageGb} format={(v) => storageLabel(Number(v))} onSelect={(v) => (storageGb = String(v))} />
				<input type="number" min="0" bind:value={storageGb} placeholder="Autre valeur (Go)" class="mt-2 w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm" />
			</div>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Type de stockage
				<select bind:value={storageType} class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal">
					<option>SSD NVMe</option><option>SSD</option><option>HDD</option><option>SSD + HDD</option><option>eMMC</option>
				</select>
			</label>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Carte graphique
				<input bind:value={gpu} list="gpuList" placeholder="Intégrée, RTX 4060 8 Go…" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
				<datalist id="gpuList">{#each specs.gpu as g (g)}<option value={g}></option>{/each}</datalist>
			</label>
			<div>
				<span class="mb-1.5 block text-sm font-semibold text-slate-700">Écran (pouces)</span>
				<Chips values={specs.screen} active={screenInches} format={(v) => `${String(v).replace('.', ',')}"`} onSelect={(v) => (screenInches = String(v))} />
				<input type="number" min="0" step="0.1" bind:value={screenInches} placeholder="Autre valeur" class="mt-2 w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm" />
			</div>
			<div>
				<span class="mb-1.5 block text-sm font-semibold text-slate-700">Système d'exploitation</span>
				<select bind:value={osValue} class="w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm">
					{#each osList as o (o)}<option value={o}>{o}</option>{/each}
					<option value="">Non précisé</option>
					<option value={OS_OTHER}>Autre…</option>
				</select>
				{#if osValue === OS_OTHER}
					<input bind:value={osOther} placeholder="Précisez le système" class="mt-2 w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm" />
				{/if}
			</div>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Clavier
				<select bind:value={keyboard} class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal">
					<option>AZERTY (FR)</option><option>QWERTY (US)</option><option>QWERTY (UK)</option><option>AZERTY (BE)</option><option>QWERTZ</option>
				</select>
			</label>
		</div>
	</div>

	{#if isUsed}
		<div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6">
			<h2 class="m-0 text-base font-bold">3. État de l'occasion</h2>
			<div>
				<span class="mb-1.5 block text-sm font-semibold text-slate-700">Condition</span>
				<div class="flex flex-wrap gap-2">
					{#each Object.entries(CONDITION_LABEL) as [value, label] (value)}
						<label
							class="flex cursor-pointer items-center gap-1.5 rounded-lg border px-3 py-1.5 text-sm"
							class:border-teal-600={condition === value}
							class:bg-teal-50={condition === value}
							class:border-slate-300={condition !== value}
						>
							<input
								type="radio"
								name="condition"
								checked={condition === value}
								onchange={() => (condition = value as Condition)}
								class="accent-teal-600"
							/> {label}
						</label>
					{/each}
				</div>
			</div>
			<div class="grid grid-cols-2 gap-4">
				<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Santé de la batterie (%)
					<input type="number" min="0" max="100" bind:value={batteryHealth} placeholder="ex. 87" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
				</label>
				<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Défauts constatés
					<input bind:value={defects} placeholder="Aucun" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
				</label>
				<label class="col-span-2 flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Accessoires fournis
					<input bind:value={accessories} list="accList" placeholder="Chargeur d'origine" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
					<datalist id="accList">
						<option value="Chargeur d'origine"></option>
						<option value="Chargeur d'origine, boîte d'origine"></option>
						<option value="Chargeur compatible"></option>
						<option value="Sans chargeur"></option>
					</datalist>
				</label>
			</div>
		</div>
	{/if}

	<div class="rounded-2xl border border-slate-200 bg-white p-6">
		<h2 class="mb-4 text-base font-bold">{isUsed ? '4' : '3'}. Photos</h2>
		<PhotoDropzone {photos} onChange={(p) => (photos = p)} />
	</div>

	<div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6">
		<h2 class="m-0 text-base font-bold">{isUsed ? '5' : '4'}. Prix et publication</h2>
		<div class="grid grid-cols-2 gap-4">
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Prix (TND)
				<input type="number" min="0" required bind:value={priceTnd} placeholder="ex. 1650" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
			</label>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Prix barré / avant promo (TND)
				<input type="number" min="0" bind:value={compareAtPriceTnd} placeholder="Laisser vide si pas de promotion" class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal" />
			</label>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Quantité
				<input type="number" min="1" disabled={isUsed} bind:value={quantity} class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal disabled:bg-slate-50" />
			</label>
			<div>
				<span class="mb-1.5 block text-sm font-semibold text-slate-700">Garantie (mois)</span>
				<Chips
					values={WARRANTY_VALUES}
					active={warrantyMonths}
					format={(v) => (Number(v) === 0 ? 'Aucune' : `${v} mois`)}
					onSelect={(v) => {
						warrantyMonths = String(v);
						warrantyTouched = true;
					}}
				/>
				<input type="number" min="0" bind:value={warrantyMonths} oninput={() => (warrantyTouched = true)} class="mt-2 w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm" />
			</div>
			<label class="flex flex-col gap-1.5 text-sm font-semibold text-slate-700">Statut
				<select bind:value={status} class="rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm font-normal">
					<option value="available">Disponible (visible en boutique)</option>
					<option value="hidden">Masqué (brouillon)</option>
					<option value="sold">Vendu</option>
				</select>
			</label>
			<div class="col-span-2">
				<div class="mb-1.5 flex items-baseline justify-between">
					<span class="text-sm font-semibold text-slate-700">Description</span>
					<button type="button" onclick={generateDescription} class="text-sm font-semibold text-teal-700">Générer à partir des caractéristiques</button>
				</div>
				<textarea bind:value={description} rows="4" placeholder="Testé en atelier, garantie boutique…" class="w-full resize-y rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm"
				></textarea>
			</div>
		</div>
	</div>

	<div class="sticky bottom-0 flex flex-wrap items-center gap-3 bg-gradient-to-t from-slate-50 pt-4 pb-2">
		<button
			bind:this={saveButton}
			type="submit"
			data-next="dashboard"
			disabled={saving}
			class="rounded-lg bg-teal-600 px-6 py-3 text-sm font-bold text-white hover:bg-teal-700 disabled:opacity-60"
		>Enregistrer</button>
		<button
			type="submit"
			data-next="another"
			disabled={saving}
			class="rounded-lg border border-teal-600 px-5 py-2.5 text-sm font-bold text-teal-700"
		>Enregistrer et ajouter un autre</button>
		<span class="text-xs text-slate-400">⌘/Ctrl + Entrée pour enregistrer</span>
		{#if error}<span class="text-sm text-red-600">{error}</span>{/if}
	</div>
</form>
