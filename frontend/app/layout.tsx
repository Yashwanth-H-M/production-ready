import type { Metadata, Viewport } from 'next';
import { ClerkProvider } from '@clerk/nextjs';
import './globals.css';
import AppShell from '../components/AppShell';
import ClerkWorkspace from '../components/ClerkWorkspace';

export const metadata: Metadata = {
  title: {
    default: 'Canopy | Nursery operations',
    template: '%s | Canopy',
  },
  description: 'Enterprise nursery operations for crop inventory, plant health, compliance, and customer commitments.',
  manifest: '/manifest.json',
  applicationName: 'Canopy Operations Cloud',
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  themeColor: '#102018',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const authProvider = process.env.NEXT_PUBLIC_AUTH_PROVIDER ?? (process.env.NODE_ENV === 'production' ? 'clerk' : 'local');
  const clerkAuthEnabled = authProvider === 'clerk';
  return (
    <html lang="en">
      <body>
        {clerkAuthEnabled ? (
          <ClerkProvider dynamic signInUrl="/sign-in" signUpUrl="/sign-up">
            <ClerkWorkspace>{children}</ClerkWorkspace>
          </ClerkProvider>
        ) : <AppShell>{children}</AppShell>}
      </body>
    </html>
  );
}
