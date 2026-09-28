// Typed fetch client — same behavior as the old web/js/api.js `api()` helper, parameterized
// so both the admin app (with a bearer token) and the storefront (no token) can share it.

export class ApiError extends Error {}

export interface ApiClientOptions {
  /** e.g. "http://localhost:8080/api" */
  baseUrl: string;
  /** Returns the current bearer token, or null/undefined if there isn't one. */
  getToken?: () => string | null | undefined;
  /** Called when the server responds 401 to an authenticated request. */
  onAuthExpired?: () => void;
  /** Used for SSR requests that need to forward cookies/headers; unused by default. */
  fetch?: typeof fetch;
}

export interface ApiClient {
  request<T>(path: string, init?: RequestInit): Promise<T>;
}

export function createApiClient(opts: ApiClientOptions): ApiClient {
  const doFetch = opts.fetch ?? fetch;

  async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const headers = new Headers(init.headers);
    if (init.body && !(init.body instanceof FormData) && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }
    const token = opts.getToken?.();
    if (token) headers.set("Authorization", `Bearer ${token}`);

    const res = await doFetch(`${opts.baseUrl}${path}`, { ...init, headers });
    if (res.status === 401 && token) opts.onAuthExpired?.();
    if (res.status === 204) return null as T;

    const data = await res.json().catch(() => null);
    if (!res.ok) {
      const message = (data && typeof data === "object" && "error" in data && data.error) || res.statusText;
      throw new ApiError(String(message));
    }
    return data as T;
  }

  return { request };
}
