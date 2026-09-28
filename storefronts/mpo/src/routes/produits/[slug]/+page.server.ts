import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { api } from '$lib/api';
import { findCategoryPath, type CategoryDto, type ProductDto } from '@pcecom/shared';

function findCategorySlugById(tree: CategoryDto[], id: number): string | null {
	for (const n of tree) {
		if (n.id === id) return n.slug;
		const found = findCategorySlugById(n.children ?? [], id);
		if (found) return found;
	}
	return null;
}

export const load: PageServerLoad = async ({ params }) => {
	let product: ProductDto;
	try {
		product = await api.request<ProductDto>(`/products/${encodeURIComponent(params.slug)}`);
	} catch {
		throw error(404, 'Produit introuvable');
	}

	let breadcrumbPath: CategoryDto[] | null = null;
	try {
		const tree = await api.request<CategoryDto[]>('/categories');
		const slug = findCategorySlugById(tree, product.categoryId);
		breadcrumbPath = slug ? findCategoryPath(tree, slug) : null;
	} catch {
		// breadcrumb is a nice-to-have — a categories fetch failure shouldn't break the product page
	}

	return { product, breadcrumbPath };
};
