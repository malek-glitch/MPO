import type { PageServerLoad } from './$types';
import { api } from '$lib/api';
import { qs, type CategoryDto, type Page, type ProductDto } from '@pcecom/shared';

const CAT_TINTS = ['blue', 'pink', 'purple', 'cyan'];

export interface CategoryTile extends CategoryDto {
	total: number;
	tint: string;
}

const emptyPage: Page<ProductDto> = { items: [], total: 0, page: 1, pageSize: 0 };

export const load: PageServerLoad = async () => {
	// Each section degrades independently — a single flaky request shouldn't 500 the whole homepage,
	// matching the old client-rendered site's per-section failure handling.
	const [heroPage, categories, newest, used, featured] = await Promise.all([
		api.request<Page<ProductDto>>('/products?' + qs({ sort: 'newest', pageSize: 1 })).catch(() => emptyPage),
		api.request<CategoryDto[]>('/categories').catch(() => [] as CategoryDto[]),
		api.request<Page<ProductDto>>('/products?' + qs({ sort: 'newest', pageSize: 4 })).catch(() => emptyPage),
		api
			.request<Page<ProductDto>>('/products?' + qs({ used: true, sort: 'newest', pageSize: 4 }))
			.catch(() => emptyPage),
		api.request<Page<ProductDto>>('/products?' + qs({ featured: true, pageSize: 8 })).catch(() => emptyPage)
	]);

	const topCategories = categories.slice(0, 4);
	const categoryTiles: CategoryTile[] = await Promise.all(
		topCategories.map(async (c, i) => {
			let total = 0;
			try {
				const p = await api.request<Page<ProductDto>>(
					'/products?' + qs({ category: c.slug, pageSize: 1, includeSold: true })
				);
				total = p.total;
			} catch {
				// keep 0 — category listing still renders without a count
			}
			return { ...c, total, tint: CAT_TINTS[i % CAT_TINTS.length] };
		})
	);

	return {
		hero: heroPage.items[0] ?? null,
		categories: categoryTiles,
		newest: newest.items,
		used: used.items,
		featured: featured.items
	};
};
