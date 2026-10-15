import { clerkMiddleware, createRouteMatcher } from '@clerk/nextjs/server';
import { NextFetchEvent, NextMiddleware, NextRequest, NextResponse } from 'next/server';

const isPublicRoute = createRouteMatcher([
  '/sign-in(.*)',
  '/sign-up(.*)',
  '/manifest.json',
  '/icons(.*)',
]);

const clerkHandler: NextMiddleware = clerkMiddleware(async (auth, request) => {
  const authProvider = process.env.NEXT_PUBLIC_AUTH_PROVIDER ?? (process.env.NODE_ENV === 'production' ? 'clerk' : 'local');
  if (authProvider === 'clerk' && !isPublicRoute(request)) await auth.protect();
}, {
  contentSecurityPolicy: { strict: true },
});

export default function proxy(request: NextRequest, event: NextFetchEvent) {
  const authProvider = process.env.NEXT_PUBLIC_AUTH_PROVIDER ?? (process.env.NODE_ENV === 'production' ? 'clerk' : 'local');
  if (authProvider !== 'clerk') return NextResponse.next();
  return clerkHandler(request, event);
}

export const config = {
  matcher: [
    '/((?!_next|[^?]*\\.(?:html?|css|js(?!on)|jpe?g|webp|png|gif|svg|ttf|woff2?|ico|csv|docx?|xlsx?|zip|webmanifest)).*)',
    '/(api|trpc)(.*)',
  ],
};
