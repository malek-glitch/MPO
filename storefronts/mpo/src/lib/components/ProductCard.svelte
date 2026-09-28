<script lang="ts">
	import { money, specLine, priceInfo, type ProductDto } from '@pcecom/shared';
	import { conditionInfo } from '$lib/badges';

	let { product }: { product: ProductDto } = $props();

	const sold = $derived(product.status === 'sold');
	const cover = $derived(product.photos?.[0]);
	const info = $derived(priceInfo(product));
	const condition = $derived(conditionInfo(product));
</script>

<a class="product-card" class:sold href={`/produits/${product.slug}`}>
	<div class="ph">
		{#if !product.isUsed}
			<span class="badge neuf">Neuf</span>
		{:else}
			<span class="badge occ"><span class="dot {condition.dotClass}"></span>Occasion · {condition.label}</span>
		{/if}
		{#if info.discountPct}
			<span class="badge promo">-{info.discountPct}%</span>
		{/if}
		{#if cover}
			<img src={cover.thumb} alt="" />
		{:else}
			photo produit
		{/if}
		{#if sold}
			<div class="sold-ribbon"><span>VENDU</span></div>
		{/if}
	</div>
	<div class="body">
		<div class="brand">{product.brand}</div>
		<div class="model">{product.model}</div>
		<div class="specs">{specLine(product)}</div>
		<div class="price-row">
			<span class="price-main">
				<span class="price">{money(info.price)}</span>
				{#if info.compareAt}<span class="price-old">{money(info.compareAt)}</span>{/if}
			</span>
			{#if product.isUsed && product.used?.batteryHealth != null}
				<span class="batt">Batt. {product.used.batteryHealth} %</span>
			{/if}
		</div>
	</div>
</a>
