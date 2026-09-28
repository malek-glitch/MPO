import { flattenCategories, type CategoryDto, type FlatCategory, type Page, type ProductDto } from '@pcecom/shared';
import { api } from '$lib/api';

class CatalogState {
	categories = $state<CategoryDto[]>([]);
	products = $state<ProductDto[]>([]);

	get flatCategories(): FlatCategory[] {
		return flattenCategories(this.categories);
	}
}

export const catalog = new CatalogState();

export async function loadCategories() {
	catalog.categories = await api.request<CategoryDto[]>('/admin/categories');
}

export async function loadProducts() {
	const page = await api.request<Page<ProductDto>>('/admin/products?page=1&pageSize=200');
	catalog.products = page.items;
}

export async function loadAll() {
	await Promise.all([loadCategories(), loadProducts()]);
}
