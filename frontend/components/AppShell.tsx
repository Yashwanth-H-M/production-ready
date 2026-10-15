'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';

type IconName = 'grid' | 'leaf' | 'box' | 'map' | 'shield' | 'pulse' | 'cart' | 'search' | 'bell' | 'chevron' | 'sparkle' | 'plus' | 'help' | 'command' | 'dots' | 'arrow';

type NavItem = { href: string; label: string; icon: IconName; count?: string };

const primaryNav: NavItem[] = [
  { href: '/', label: 'Command center', icon: 'grid' },
  { href: '/batches', label: 'Crop inventory', icon: 'box' },
  { href: '/zones', label: 'Sites & zones', icon: 'map' },
  { href: '/orders', label: 'Revenue pipeline', icon: 'cart' },
];

const controlNav: NavItem[] = [
  { href: '/treatments', label: 'Compliance', icon: 'shield' },
  { href: '/diagnostics', label: 'Plant health', icon: 'pulse' },
];

const routeNames: Record<string, string> = {
  '/': 'Command center',
  '/batches': 'Crop inventory',
  '/zones': 'Sites & zones',
  '/treatments': 'Compliance register',
  '/diagnostics': 'Plant health review',
  '/orders': 'Revenue pipeline',
};

type AppShellProps = {
  children: ReactNode;
  organizationName?: string;
  userName?: string;
  organizationSwitcher?: ReactNode;
  profileMenu?: ReactNode;
};

export default function AppShell({
  children,
  organizationName = 'Green Valley Nursery',
  userName = 'Workspace user',
  organizationSwitcher,
  profileMenu,
}: AppShellProps) {
  const pathname = usePathname();
  const [online, setOnline] = useState(true);

  useEffect(() => {
    const syncNetwork = () => setOnline(window.navigator.onLine);
    syncNetwork();
    window.addEventListener('online', syncNetwork);
    window.addEventListener('offline', syncNetwork);
    return () => { window.removeEventListener('online', syncNetwork); window.removeEventListener('offline', syncNetwork); };
  }, []);

  const isNew = pathname?.endsWith('/new');
  const currentName = isNew
    ? pathname?.includes('batches') ? 'Create crop batch' : pathname?.includes('treatments') ? 'Log application' : pathname?.includes('diagnostics') ? 'Capture observation' : 'Create customer order'
    : routeNames[pathname ?? '/'] ?? 'Workspace';

  return (
    <div className="min-h-screen bg-[var(--app-bg)] text-slate-950">
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-[258px] flex-col border-r border-white/10 bg-[#101A1A] text-white xl:flex">
        <div className="px-5 pb-5 pt-6">
          <Link href="/" className="group flex items-center gap-3" aria-label="Canopy command center">
            <span className="grid h-10 w-10 place-items-center rounded-xl bg-[#D9FF5A] text-[#102018] shadow-[0_6px_20px_rgba(217,255,90,0.16)] transition-transform group-hover:rotate-[-5deg]"><Icon name="leaf" className="h-5 w-5" /></span>
            <span><span className="block font-brand text-[21px] font-bold leading-5 tracking-[-0.055em]">canopy</span><span className="mt-1 block text-[9px] font-bold uppercase tracking-[0.19em] text-white/45">Operations cloud</span></span>
          </Link>
        </div>

        <div className="mx-4 h-px bg-white/10" />
        <div className="flex-1 overflow-y-auto px-3 py-5">
          <NavGroup label="Operate" items={primaryNav} pathname={pathname} />
          <NavGroup label="Control" items={controlNav} pathname={pathname} />
        </div>

        <div className="border-t border-white/10 p-3">
          <div className="rounded-xl bg-white/[0.055] p-3">
            <div className="flex items-center gap-2.5"><span className="grid h-8 w-8 place-items-center rounded-lg bg-gradient-to-br from-[#D9FF5A] to-[#68B884] font-data text-[10px] font-bold text-[#112219]">{initials(organizationName)}</span><span className="min-w-0 flex-1"><span className="block truncate text-xs font-semibold text-white">{organizationName}</span><span className="mt-0.5 block text-[10px] text-white/45">Active organization</span></span></div>
            <div className="mt-3 flex items-center gap-2 border-t border-white/10 pt-3 text-[10px] font-medium text-white/55"><span className={`h-1.5 w-1.5 rounded-full ${online ? 'bg-[#D9FF5A] shadow-[0_0_0_4px_rgba(217,255,90,0.08)]' : 'bg-orange-300'}`} />{online ? 'System synchronized' : 'Offline workspace'}</div>
          </div>
        </div>
      </aside>

      <div className="min-h-screen xl:pl-[258px]">
        <header className="sticky top-0 z-30 flex h-[72px] items-center justify-between border-b border-slate-200/80 bg-[var(--app-bg)]/88 px-4 backdrop-blur-xl sm:px-6 xl:px-8">
          <div className="flex min-w-0 items-center gap-3">
            <Link href="/" className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-[#102018] text-[#D9FF5A] xl:hidden"><Icon name="leaf" className="h-5 w-5" /></Link>
            <div className="min-w-0"><p className="hidden truncate text-[10px] font-bold uppercase tracking-[0.15em] text-slate-400 sm:block">{organizationName} / Operations</p><h1 className="truncate font-brand text-xl font-bold tracking-[-0.05em] text-[#18252A] sm:text-[23px]">{currentName}</h1></div>
          </div>
          <div className="flex items-center gap-2.5">
            {organizationSwitcher && <div className="max-w-[11rem] sm:max-w-[15rem]">{organizationSwitcher}</div>}
            <span className="hidden items-center gap-2 text-xs font-semibold text-slate-500 md:flex"><span className={`h-2 w-2 rounded-full ${online ? 'bg-emerald-500' : 'bg-orange-400'}`} />Network {online ? 'online' : 'offline'}</span>
            {profileMenu ?? <span className="grid h-10 w-10 place-items-center rounded-xl bg-[#18252A] text-xs font-bold text-white" aria-label={userName}>{initials(userName)}</span>}
          </div>
        </header>
        <main className="mx-auto w-full max-w-[1680px] px-4 pb-24 pt-6 sm:px-6 xl:px-8 xl:pb-8 xl:pt-8">{children}</main>
      </div>

      <nav className="fixed inset-x-0 bottom-0 z-40 grid grid-cols-5 border-t border-slate-200 bg-white/95 px-1 pb-[max(0.35rem,env(safe-area-inset-bottom))] pt-1 backdrop-blur-xl xl:hidden" aria-label="Mobile navigation">
        {[...primaryNav.slice(0, 3), ...controlNav.slice(0, 1), { href: '/orders', label: 'More', icon: 'dots' as IconName }].map((item) => {
          const active = item.href === '/' ? pathname === '/' : pathname?.startsWith(item.href);
          return <Link key={`${item.href}-${item.label}`} href={item.href} className={`flex min-h-[52px] flex-col items-center justify-center gap-1 rounded-lg text-[10px] font-semibold ${active ? 'text-[#0F7C61]' : 'text-slate-400'}`}><Icon name={item.icon} className="h-[18px] w-[18px]" /><span>{item.label.split(' ')[0]}</span></Link>;
        })}
      </nav>
    </div>
  );
}

function initials(value: string) {
  return value.trim().split(/\s+/).slice(0, 2).map((part) => part.charAt(0).toUpperCase()).join('') || 'CO';
}

function NavGroup({ label, items, pathname }: { label: string; items: NavItem[]; pathname: string | null }) {
  return <div className="mb-7"><p className="mb-2 px-3 text-[9px] font-bold uppercase tracking-[0.17em] text-white/35">{label}</p><div className="space-y-1">{items.map((item) => { const active = item.href === '/' ? pathname === '/' : pathname?.startsWith(item.href); return <Link key={item.href} href={item.href} className={`group flex items-center gap-3 rounded-xl px-3 py-2.5 text-[13px] font-semibold transition ${active ? 'bg-white text-[#162424] shadow-[0_5px_15px_rgba(0,0,0,0.12)]' : 'text-white/58 hover:bg-white/[0.07] hover:text-white'}`}><Icon name={item.icon} className={`h-[17px] w-[17px] ${active ? 'text-[#0F7C61]' : 'text-white/42 group-hover:text-white'}`} /><span className="flex-1">{item.label}</span>{item.count && <span className={`rounded-md px-1.5 py-0.5 font-data text-[10px] ${active ? 'bg-[#FFF1E9] text-[#BF482E]' : 'bg-white/10 text-white/60'}`}>{item.count}</span>}</Link>; })}</div></div>;
}

function Icon({ name, className }: { name: IconName; className?: string }) {
  const attrs = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.85, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const };
  const paths: Record<IconName, React.ReactNode> = {
    grid: <><rect x="3.5" y="3.5" width="6.5" height="6.5" rx="1" /><rect x="14" y="3.5" width="6.5" height="6.5" rx="1" /><rect x="3.5" y="14" width="6.5" height="6.5" rx="1" /><rect x="14" y="14" width="6.5" height="6.5" rx="1" /></>,
    leaf: <><path d="M20 4C10 4 4 9 4 16c0 2.3 1.5 4 4.2 4C15 20 20 13.5 20 4Z" /><path d="M4.5 20c2.5-4.2 6-7.2 11-10.2" /></>,
    box: <><path d="m4 7 8-4 8 4v10l-8 4-8-4V7Z" /><path d="m4 7 8 4.2L20 7M12 11.2V21" /></>,
    map: <><path d="m4 6 5-2 6 2 5-2v14l-5 2-6-2-5 2V6Z" /><path d="M9 4v14M15 6v14" /></>,
    shield: <><path d="M12 3.5 19 6v5.5c0 4.5-2.9 7.8-7 9-4.1-1.2-7-4.5-7-9V6l7-2.5Z" /><path d="m9 12 2 2 4-4" /></>,
    pulse: <><path d="M3 12h4l2-5 4 10 2-5h6" /><path d="M20 5.5a8.5 8.5 0 1 0 0 13" /></>,
    cart: <><path d="M3 4h2l2.2 10.3a2 2 0 0 0 2 1.7h7.6a2 2 0 0 0 1.9-1.4L20 9H7" /><circle cx="10" cy="20" r="1" /><circle cx="17" cy="20" r="1" /></>,
    search: <><circle cx="10.5" cy="10.5" r="5.5" /><path d="m15 15 4.5 4.5" /></>,
    bell: <><path d="M18 10a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 22h4" /></>,
    chevron: <path d="m8 10 4 4 4-4" />,
    sparkle: <><path d="m12 3 1.4 5.6L19 10l-5.6 1.4L12 17l-1.4-5.6L5 10l5.6-1.4L12 3Z" /><path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7L19 16Z" /></>,
    plus: <path d="M12 5v14M5 12h14" />,
    help: <><circle cx="12" cy="12" r="8.5" /><path d="M9.5 9a2.6 2.6 0 1 1 4.3 2c-1.3 1-1.8 1.5-1.8 3" /><path d="M12 17h.01" /></>,
    command: <><path d="M8.5 9.5a3 3 0 1 1-3-3h1a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2h-1a3 3 0 1 1 3-3" /><path d="M15.5 14.5a3 3 0 1 1 3 3h-1a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h1a3 3 0 1 1-3 3" /></>,
    dots: <><circle cx="5" cy="12" r="1" fill="currentColor" /><circle cx="12" cy="12" r="1" fill="currentColor" /><circle cx="19" cy="12" r="1" fill="currentColor" /></>,
    arrow: <><path d="M5 12h13M13 7l5 5-5 5" /></>,
  };
  return <svg viewBox="0 0 24 24" className={className} aria-hidden="true" {...attrs}>{paths[name]}</svg>;
}

export { Icon };
