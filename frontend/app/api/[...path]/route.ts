import { NextRequest, NextResponse } from 'next/server';
import { auth } from '@clerk/nextjs/server';

export const dynamic = 'force-dynamic';

const API_BASE_URL = process.env.API_INTERNAL_URL ?? (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : null);
const REQUEST_BODY_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

async function proxy(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const { path } = await context.params;
  if (!API_BASE_URL) {
    return NextResponse.json(
      { message: 'The workspace data service is not configured.' },
      { status: 503, headers: { 'Cache-Control': 'no-store' } },
    );
  }
  const url = new URL(`/api/${path.map(encodeURIComponent).join('/')}`, API_BASE_URL);
  url.search = request.nextUrl.search;

  const headers = new Headers(request.headers);
  headers.delete('host');
  headers.delete('connection');
  headers.delete('cookie');
  headers.set('x-forwarded-host', request.headers.get('host') ?? '');
  headers.set('x-forwarded-proto', request.nextUrl.protocol.replace(':', ''));

  const authProvider = process.env.AUTH_PROVIDER ?? (process.env.NODE_ENV === 'production' ? 'clerk' : 'local');
  if (authProvider === 'clerk') {
    const session = await auth();
    if (!session.userId) {
      return NextResponse.json({ message: 'Sign in to access this workspace.' }, { status: 401, headers: { 'Cache-Control': 'no-store' } });
    }
    if (!session.orgId) {
      return NextResponse.json({ message: 'Select or create a nursery organization before continuing.' }, { status: 403, headers: { 'Cache-Control': 'no-store' } });
    }
    const token = await session.getToken();
    if (!token) {
      return NextResponse.json({ message: 'The secure session token could not be issued.' }, { status: 401, headers: { 'Cache-Control': 'no-store' } });
    }
    headers.set('Authorization', `Bearer ${token}`);
  }

  try {
    const upstream = await fetch(url, {
      method: request.method,
      headers,
      body: REQUEST_BODY_METHODS.has(request.method) ? await request.arrayBuffer() : undefined,
      cache: 'no-store',
      redirect: 'manual',
    });
    const responseHeaders = new Headers(upstream.headers);
    responseHeaders.delete('connection');
    responseHeaders.delete('transfer-encoding');
    return new NextResponse(upstream.body, { status: upstream.status, headers: responseHeaders });
  } catch {
    return NextResponse.json(
      { message: 'The workspace data service is temporarily unavailable. Please retry shortly.' },
      { status: 503, headers: { 'Cache-Control': 'no-store' } },
    );
  }
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
