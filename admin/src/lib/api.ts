import { createApiClient } from '@pcecom/shared';
import { PUBLIC_API_BASE } from '$env/static/public';
import { auth, clearAuth } from './state/auth.svelte';

export const api = createApiClient({
	baseUrl: PUBLIC_API_BASE,
	getToken: () => auth.token,
	onAuthExpired: () => clearAuth()
});
