<script lang="ts">
	import { goto } from '$app/navigation';
	import { api } from '$lib/api';
	import { auth, setToken } from '$lib/state/auth.svelte';
	import type { LoginResponse, MeResponse } from '@pcecom/shared';

	let email = $state('');
	let password = $state('');
	let showPassword = $state(false);
	let error = $state('');
	let loading = $state(false);

	$effect(() => {
		if (auth.token) goto('/dashboard');
	});

	async function submit(e: SubmitEvent) {
		e.preventDefault();
		error = '';
		loading = true;
		try {
			const { token } = await api.request<LoginResponse>('/admin/login', {
				method: 'POST',
				body: JSON.stringify({ email, password })
			});
			setToken(token);
			auth.me = await api.request<MeResponse>('/admin/me');
			await goto('/dashboard');
		} catch (err) {
			error = err instanceof Error ? err.message : 'Erreur de connexion';
		} finally {
			loading = false;
		}
	}
</script>

<div class="flex min-h-screen items-center justify-center bg-slate-50 px-6">
	<div class="w-full max-w-[420px]">
		<div class="mb-6 flex items-center gap-3">
			<div class="flex h-11 w-11 items-center justify-center rounded-xl bg-teal-600 font-bold text-white">PC</div>
			<div>
				<h1 class="m-0 text-lg font-bold">Back office</h1>
				<p class="m-0 text-sm text-slate-500">Administration</p>
			</div>
		</div>
		<div class="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
			<h2 class="mb-1 text-xl font-bold">Connexion</h2>
			<p class="mb-6 text-sm text-slate-500">Accès réservé à l'équipe de la boutique.</p>
			<form onsubmit={submit} class="space-y-4">
				<div>
					<label for="email" class="mb-1.5 block text-sm font-semibold text-slate-700">Adresse e-mail</label>
					<input
						id="email"
						type="email"
						required
						bind:value={email}
						autocomplete="username"
						class="w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm focus:border-teal-600 focus:ring-2 focus:ring-teal-100 focus:outline-none"
					/>
				</div>
				<div>
					<label for="password" class="mb-1.5 block text-sm font-semibold text-slate-700">Mot de passe</label>
					<div class="relative">
						<input
							id="password"
							type={showPassword ? 'text' : 'password'}
							required
							bind:value={password}
							autocomplete="current-password"
							class="w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-sm focus:border-teal-600 focus:ring-2 focus:ring-teal-100 focus:outline-none"
						/>
						<button
							type="button"
							onclick={() => (showPassword = !showPassword)}
							class="absolute top-1/2 right-3 -translate-y-1/2 text-xs font-semibold text-slate-500"
						>{showPassword ? 'Masquer' : 'Afficher'}</button>
					</div>
				</div>
				<button
					type="submit"
					disabled={loading}
					class="w-full rounded-lg bg-teal-600 py-3 text-sm font-bold text-white transition-colors hover:bg-teal-700 disabled:opacity-60"
				>{loading ? 'Connexion…' : 'Se connecter'}</button>
				{#if error}<p class="text-sm text-red-600">{error}</p>{/if}
			</form>
		</div>
	</div>
</div>
