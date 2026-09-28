import { browser } from '$app/environment';
import type { MeResponse } from '@pcecom/shared';

const TOKEN_KEY = 'pcecom_token';

function readToken(): string | null {
	if (!browser) return null;
	return localStorage.getItem(TOKEN_KEY);
}

class AuthState {
	token = $state<string | null>(readToken());
	me = $state<MeResponse | null>(null);
}

export const auth = new AuthState();

export function setToken(token: string | null) {
	auth.token = token;
	if (browser) {
		if (token) localStorage.setItem(TOKEN_KEY, token);
		else localStorage.removeItem(TOKEN_KEY);
	}
}

export function clearAuth() {
	setToken(null);
	auth.me = null;
}
