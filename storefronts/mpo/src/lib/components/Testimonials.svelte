<script lang="ts">
	import FacebookIcon from './FacebookIcon.svelte';
	import { TESTIMONIALS } from '$lib/testimonials';

	let { facebookUrl }: { facebookUrl: string | null } = $props();

	const avg = TESTIMONIALS.reduce((sum, t) => sum + t.rating, 0) / TESTIMONIALS.length;
	const stars = (n: number) => '★'.repeat(n) + '☆'.repeat(5 - n);
</script>

<div class="section">
	<span class="section-eyebrow">Avis clients</span>
	<div class="section-head">
		<div>
			<h2>Ce que disent nos clients</h2>
			<div class="fb-rating">
				<FacebookIcon size={16} />
				<span class="stars" aria-hidden="true">{stars(Math.round(avg))}</span>
				<strong>{avg.toFixed(1)}/5</strong>
				<span class="muted">· avis Facebook</span>
			</div>
		</div>
		{#if facebookUrl}
			<a class="see-all" href={facebookUrl} target="_blank" rel="noopener">Voir tous les avis →</a>
		{/if}
	</div>
	<div class="testimonial-grid">
		{#each TESTIMONIALS as t (t.name)}
			<div class="testimonial-card">
				<div class="testimonial-head">
					<span class="avatar" style={`background:${t.color}`}>{t.initials}</span>
					<div>
						<div class="t-name">{t.name}</div>
						<div class="t-date">{t.date}</div>
					</div>
					<FacebookIcon size={15} />
				</div>
				<div class="t-stars" aria-hidden="true">{stars(t.rating)}</div>
				<p class="t-text">{t.text}</p>
			</div>
		{/each}
	</div>
</div>
