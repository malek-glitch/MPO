import { PUBLIC_API_BASE } from '$env/static/public';
import { createApiClient } from '@pcecom/shared';

/** The storefront is read-only and unauthenticated — no token, no auth-expired handling. */
export const api = createApiClient({ baseUrl: PUBLIC_API_BASE });
