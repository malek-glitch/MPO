<script lang="ts">
	import type { SidebarCategory } from '$lib/types';

	let {
		nodes,
		activeSlug,
		totalAll
	}: { nodes: SidebarCategory[]; activeSlug: string; totalAll: number } = $props();
</script>

<aside class="cat-sidebar">
	<h4>Catégories</h4>
	<ul class="cat-list">
		<li>
			<a class="cat-row" class:active={!activeSlug} href="/produits">
				<span>Tous les ordinateurs</span><span class="count">{totalAll}</span>
			</a>
		</li>
		{#each nodes as node (node.id)}
			<li>
				<a class="cat-row" class:active={activeSlug === node.slug} href={`/produits?category=${encodeURIComponent(node.slug)}`}>
					<span>{node.name}</span><span class="count">{node.count}</span>
				</a>
				{#if node.children.length}
					<ul>
						{#each node.children as child (child.id)}
							<li>
								<a class="cat-row" class:active={activeSlug === child.slug} href={`/produits?category=${encodeURIComponent(child.slug)}`}>
									<span>{child.name}</span><span class="count">{child.count}</span>
								</a>
							</li>
						{/each}
					</ul>
				{/if}
			</li>
		{/each}
	</ul>
</aside>
