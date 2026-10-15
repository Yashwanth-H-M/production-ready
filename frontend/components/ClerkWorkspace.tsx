'use client';

import { OrganizationList, OrganizationSwitcher, UserButton, useAuth, useOrganization, useUser } from '@clerk/nextjs';
import AppShell from './AppShell';

export default function ClerkWorkspace({ children }: { children: React.ReactNode }) {
  const { isLoaded, isSignedIn, orgId } = useAuth();
  const { organization } = useOrganization();
  const { user } = useUser();

  if (!isLoaded) {
    return <main className="grid min-h-screen place-items-center bg-[var(--app-bg)] px-5"><p className="text-sm font-semibold text-slate-500">Loading your secure workspace…</p></main>;
  }

  if (!isSignedIn) return null;

  if (!orgId) {
    return (
      <main className="min-h-screen bg-[var(--app-bg)] px-5 py-12 sm:px-8">
        <div className="mx-auto max-w-5xl">
          <p className="e-eyebrow text-[#0F7C61]">Canopy Operations Cloud</p>
          <h1 className="mt-2 font-brand text-3xl font-extrabold tracking-[-0.05em] text-[#18252A]">Choose or create a nursery workspace</h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">Operational records are separated by organization. Select an existing workspace or create one to continue.</p>
          <div className="mt-8"><OrganizationList afterCreateOrganizationUrl="/" afterSelectOrganizationUrl="/" hidePersonal /></div>
        </div>
      </main>
    );
  }

  return (
    <AppShell
      organizationName={organization?.name ?? 'Nursery workspace'}
      userName={user?.fullName ?? user?.primaryEmailAddress?.emailAddress ?? 'Workspace user'}
      organizationSwitcher={<OrganizationSwitcher afterCreateOrganizationUrl="/" afterSelectOrganizationUrl="/" hidePersonal />}
      profileMenu={<UserButton />}
    >
      {children}
    </AppShell>
  );
}
