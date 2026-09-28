<script lang="ts">
	import { toast } from '$lib/state/toast.svelte';

	interface PickedPhoto {
		file: File;
		url: string;
	}

	let { photos, onChange }: { photos: PickedPhoto[]; onChange: (photos: PickedPhoto[]) => void } = $props();

	let dragging = $state(false);

	function addFiles(files: FileList | null) {
		if (!files) return;
		const images = Array.from(files).filter((f) => f.type.startsWith('image/'));
		const room = 12 - photos.length;
		if (images.length > room) toast.show(`12 photos maximum — ${images.length - room} ignorée(s)`);
		const added = images.slice(0, Math.max(room, 0)).map((file) => ({ file, url: URL.createObjectURL(file) }));
		onChange([...photos, ...added]);
	}

	function remove(i: number) {
		URL.revokeObjectURL(photos[i].url);
		onChange(photos.filter((_, idx) => idx !== i));
	}

	function makeMain(i: number) {
		if (i === 0) return;
		const next = [...photos];
		const [picked] = next.splice(i, 1);
		next.unshift(picked);
		onChange(next);
	}
</script>

<label
	class="flex cursor-pointer flex-col items-center gap-1 rounded-xl border-2 border-dashed p-6 text-center text-sm text-slate-500"
	class:border-teal-600={dragging}
	class:bg-teal-50={dragging}
	class:border-slate-300={!dragging}
	ondragover={(e) => {
		e.preventDefault();
		dragging = true;
	}}
	ondragleave={() => (dragging = false)}
	ondrop={(e) => {
		e.preventDefault();
		dragging = false;
		addFiles(e.dataTransfer?.files ?? null);
	}}
>
	<input
		type="file"
		accept="image/*"
		multiple
		class="hidden"
		onchange={(e) => {
			const input = e.currentTarget;
			addFiles(input.files);
			input.value = '';
		}}
	/>
	<strong class="text-slate-700">Glissez vos photos ici</strong>
	<span>ou cliquez pour les choisir · 12 photos max · la première est la photo principale</span>
</label>

{#if photos.length}
	<div class="mt-3.5 grid gap-2.5" style="grid-template-columns: repeat(auto-fill, minmax(100px, 1fr))">
		{#each photos as p, i (p.url)}
			<div
				class="relative aspect-square cursor-pointer overflow-hidden rounded-lg border-2 bg-slate-100"
				class:border-teal-600={i === 0}
				class:border-transparent={i !== 0}
				onclick={() => makeMain(i)}
				role="button"
				tabindex="0"
				onkeydown={(e) => e.key === 'Enter' && makeMain(i)}
				title={i === 0 ? 'Photo principale' : 'Cliquer pour en faire la photo principale'}
			>
				<img src={p.url} alt="" class="h-full w-full object-cover" />
				<span class="absolute bottom-1.5 left-1.5 rounded-full bg-slate-900/70 px-1.5 py-0.5 text-[10px] font-bold text-white">
					{i === 0 ? 'Principale' : i + 1}
				</span>
				<button
					type="button"
					onclick={(e) => {
						e.stopPropagation();
						remove(i);
					}}
					class="absolute top-1 right-1 flex h-5 w-5 items-center justify-center rounded-full bg-slate-900/70 text-xs text-white"
					title="Retirer"
				>&times;</button>
			</div>
		{/each}
	</div>
{/if}
