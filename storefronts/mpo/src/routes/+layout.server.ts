import type { LayoutServerLoad } from './$types';
import { api } from '$lib/api';
import type { StoreSettingsDto } from '@pcecom/shared';

const FALLBACK_STORE: StoreSettingsDto = {
	name: 'Meilleurs Prix Ordinateur',
	logoUrl: null,
	whatsappNumber: '',
	phone: null,
	address: null,
	facebookUrl: null,
	instagramUrl: null,
	tiktokUrl: null,
	whatsappTemplate: null,
	deliveryInfo: null
};

export const load: LayoutServerLoad = async () => {
	// Ported from the old getStore()'s fallback: keep the site usable if the API is briefly down.
	const store = await api.request<StoreSettingsDto>('/store').catch(() => FALLBACK_STORE);
	return { store };
};
