import type { PageServerLoad } from './$types';
import { api } from '$lib/api';
import { qs, findCategoryPath, type CategoryDto, type Page, type ProductDto } from '@pcecom/shared';
import type { SidebarCategory } from '$lib/types';

const PAGE_SIZE = 12;

async function countFor(slug: string): Promise<number> {
	try {
		const p = await api.request<Page<ProductDto>>('/products?' + qs({ category: slug, pageSize: 1, includeSold: true }));
		return p.total;
	} catch {
		return 0;
	}
}

export const load: PageServerLoad = async ({ url }) => {
	const category = url.searchParams.get('category') ?? '';
	const used = url.searchParams.get('used') ?? '';
	const q = url.searchParams.get('q') ?? '';
	const brand = url.searchParams.get('brand') ?? '';
	const ram = url.searchParams.get('ram') ?? '';
	const storageMin = url.searchParams.get('storageMin') ?? '';
	const screenMin = url.searchParams.get('screenMin') ?? '';
	const screenMax = url.searchParams.get('screenMax') ?? '';
	const priceMin = url.searchParams.get('priceMin') ?? '';
	const priceMax = url.searchParams.get('priceMax') ?? '';
	const sort = (url.searchParams.get('sort') as 'newest' | 'price_asc' | 'price_desc') || 'newest';
	const page = Math.max(1, Number(url.searchParams.get('page') || 1));

	const emptyPage: Page<ProductDto> = { items: [], total: 0, page: 1, pageSize: PAGE_SIZE };

	// Each piece degrades independently — a flaky sidebar/filter request shouldn't 500 the whole
	// listing page, and even a failed product query renders as an empty grid rather than a hard error.
	const [categoryTree, brands, totalAllPage, productsPage] = await Promise.all([
		api.request<CategoryDto[]>('/categories').catch(() => [] as CategoryDto[]),
		api.request<string[]>('/brands').catch(() => [] as string[]),
		api.request<Page<ProductDto>>('/products?' + qs({ pageSize: 1, includeSold: true })).catch(() => emptyPage),
		api
			.request<Page<ProductDto>>(
				'/products?' +
					qs({
						category,
						used,
						q,
						brand,
						ram,
						storageMin,
						screenMin,
						screenMax,
						priceMin,
						priceMax,
						sort,
						page,
						pageSize: PAGE_SIZE,
						includeSold: true
					})
			)
			.catch(() => emptyPage)
	]);

	const sidebar: SidebarCategory[] = await Promise.all(
		categoryTree.map(async (node) => ({
			id: node.id,
			slug: node.slug,
			name: node.name,
			count: await countFor(node.slug),
			children: await Promise.all(
				(node.children ?? []).map(async (child) => ({
					id: child.id,
					slug: child.slug,
					name: child.name,
					count: await countFor(child.slug),
					children: [] as SidebarCategory[]
				}))
			)
		}))
	);

	let title = 'Tous les ordinateurs';
	if (category) {
		const path = findCategoryPath(categoryTree, category);
		title = path ? path[path.length - 1].name : 'Ordinateurs';
	} else if (used === 'true') {
		title = "PC & Mac d'occasion";
	} else if (used === 'false') {
		title = 'PC & Mac neufs';
	}

	return {
		filters: { category, used, q, brand, ram, storageMin, screenMin, screenMax, priceMin, priceMax, sort, page },
		title,
		sidebar,
		totalAll: totalAllPage.total,
		brands,
		products: productsPage.items,
		total: productsPage.total,
		pageSize: PAGE_SIZE
	};
};
