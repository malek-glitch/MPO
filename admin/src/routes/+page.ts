import { redirect } from '@sveltejs/kit';
import { browser } from '$app/environment';
import { auth } from '$lib/state/auth.svelte';

export function load() {
	if (browser) {
		throw redirect(307, auth.token ? '/dashboard' : '/login');
	}
	return {};
}
