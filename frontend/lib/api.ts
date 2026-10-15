import { getCache, setCache } from './offlineSync';

const LOCAL_TOKEN_KEY = 'canopy.localAccessToken';
const DEMO_TENANT_ID = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
const DEMO_USER_ID = 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
const CACHE_PREFIX = 'api-cache:';
const authProvider = process.env.NEXT_PUBLIC_AUTH_PROVIDER ?? (process.env.NODE_ENV === 'production' ? 'clerk' : 'local');
const clerkAuthEnabled = authProvider === 'clerk';

export const demoModeEnabled = authProvider === 'local' && process.env.NEXT_PUBLIC_DEMO_MODE === 'true';

export class ApiError extends Error {
  constructor(message: string, public readonly status: number) {
    super(message);
    this.name = 'ApiError';
    Object.setPrototypeOf(this, ApiError.prototype);
  }
}

function canUseBrowserStorage() {
  return typeof window !== 'undefined' && typeof window.indexedDB !== 'undefined';
}

async function getLocalAccessToken(): Promise<string> {
  if (typeof window === 'undefined') throw new ApiError('This request requires a browser session.', 401);
  const existing = window.localStorage.getItem(LOCAL_TOKEN_KEY);
  if (existing) return existing;

  const response = await fetch('/api/auth/local-session', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin@greenvalley.com', tenantId: DEMO_TENANT_ID, userId: DEMO_USER_ID }),
  });
  if (!response.ok) throw new ApiError('The workspace session could not be started. Sign in through the configured identity provider.', response.status);

  const payload = await response.json() as { token?: string };
  if (!payload.token) throw new ApiError('The workspace session returned an invalid response.', 500);
  window.localStorage.setItem(LOCAL_TOKEN_KEY, payload.token);
  return payload.token;
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const isRead = (init.method ?? 'GET').toUpperCase() === 'GET';
  const cacheKey = `${CACHE_PREFIX}${path}`;
  try {
    const headers = new Headers(init.headers);
    headers.set('Accept', 'application/json');
    if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

    if (!clerkAuthEnabled) {
      const token = await getLocalAccessToken();
      headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(path, { ...init, headers });
    if (!response.ok) {
      const payload = await response.json().catch(() => null) as { message?: string; error?: string } | null;
      throw new ApiError(payload?.message ?? payload?.error ?? `Request failed (${response.status}).`, response.status);
    }
    if (response.status === 204) return undefined as T;
    const payload = await response.json() as T;
    if (!clerkAuthEnabled && isRead && canUseBrowserStorage()) void setCache(cacheKey, payload).catch(() => undefined);
    return payload;
  } catch (error) {
    if (!clerkAuthEnabled && isRead && canUseBrowserStorage()) {
      const cached = await getCache(cacheKey).catch(() => null);
      if (cached !== null) return cached as T;
    }
    throw error;
  }
}

export function clearLocalSession() {
  if (typeof window !== 'undefined') window.localStorage.removeItem(LOCAL_TOKEN_KEY);
}
