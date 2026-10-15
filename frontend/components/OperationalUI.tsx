import Link from 'next/link';
import { Icon } from './AppShell';

type Tone = 'success' | 'attention' | 'critical' | 'neutral' | 'info';

const toneStyles: Record<Tone, string> = {
  success: 'border-emerald-100 bg-emerald-50 text-emerald-700',
  attention: 'border-amber-100 bg-amber-50 text-amber-700',
  critical: 'border-red-100 bg-red-50 text-red-700',
  neutral: 'border-slate-200 bg-slate-50 text-slate-600',
  info: 'border-cyan-100 bg-cyan-50 text-cyan-700',
};

export function PageHeader({ eyebrow, title, description, action, trail }: { eyebrow?: string; title: string; description?: string; action?: React.ReactNode; trail?: string }) {
  return <div className="flex flex-col gap-5 border-b border-slate-200/80 pb-6 lg:flex-row lg:items-end lg:justify-between"><div className="max-w-3xl">{trail && <p className="mb-2 text-xs font-medium text-slate-400">{trail}</p>}{eyebrow && <p className="e-eyebrow mb-2">{eyebrow}</p>}<h2 className="e-title text-[30px] leading-[1.05] sm:text-[36px]">{title}</h2>{description && <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-500">{description}</p>}</div>{action && <div className="shrink-0">{action}</div>}</div>;
}

export function PrimaryLink({ href, children, icon = 'plus' }: { href: string; children: React.ReactNode; icon?: Parameters<typeof Icon>[0]['name'] }) {
  return <Link href={href} className="e-primary"><Icon name={icon} className="h-4 w-4" />{children}</Link>;
}

export function MetricCard({ label, value, delta, detail, tone = 'neutral', icon }: { label: string; value: string | number; delta?: string; detail: string; tone?: Tone; icon?: Parameters<typeof Icon>[0]['name'] }) {
  const accent: Record<Tone, string> = { success: 'bg-emerald-500', attention: 'bg-amber-500', critical: 'bg-red-500', neutral: 'bg-slate-400', info: 'bg-cyan-500' };
  return <div className="e-panel relative overflow-hidden p-5"><div className={`absolute inset-x-0 top-0 h-1 ${accent[tone]}`} /><div className="flex items-start justify-between"><p className="e-eyebrow">{label}</p>{icon && <span className="grid h-9 w-9 place-items-center rounded-xl bg-slate-50 text-slate-500"><Icon name={icon} className="h-[18px] w-[18px]" /></span>}</div><div className="mt-5 flex items-end gap-2"><p className="font-brand text-[30px] font-bold leading-none tracking-[-0.06em] text-[#18252A]">{value}</p>{delta && <span className={`mb-0.5 text-xs font-bold ${tone === 'critical' ? 'text-red-600' : tone === 'attention' ? 'text-amber-600' : 'text-emerald-600'}`}>{delta}</span>}</div><p className="mt-2 text-xs leading-5 text-slate-500">{detail}</p></div>;
}

export function StatusBadge({ label, tone = 'neutral', dot = true }: { label: string; tone?: Tone; dot?: boolean }) {
  return <span className={`inline-flex items-center gap-1.5 rounded-lg border px-2 py-1 text-[11px] font-bold ${toneStyles[tone]}`}>{dot && <span className="h-1.5 w-1.5 rounded-full bg-current" />}{label}</span>;
}

export function StageTrack({ status }: { status: string }) {
  const stages = ['propagating', 'growing', 'mature', 'sold'];
  const current = stages.indexOf(status);
  const dead = status === 'dead';
  return <div className="flex min-w-[154px] items-center gap-1.5" aria-label={`Lifecycle stage: ${status}`}><span className="flex gap-1">{stages.map((stage, index) => <span key={stage} className={`h-1.5 w-6 rounded-full ${dead && index <= 1 ? 'bg-red-400' : !dead && index <= current ? 'bg-[#0F7C61]' : 'bg-slate-200'}`} />)}</span><span className={`ml-1 text-[10px] font-bold capitalize ${dead ? 'text-red-600' : 'text-slate-500'}`}>{status}</span></div>;
}

export function StatePanel({ title, description, action, type = 'empty' }: { title: string; description: string; action?: { href: string; label: string }; type?: 'empty' | 'error' }) {
  const error = type === 'error';
  return <div className="e-panel flex min-h-[290px] flex-col items-center justify-center px-6 py-12 text-center"><span className={`grid h-12 w-12 place-items-center rounded-2xl ${error ? 'bg-red-50 text-red-600' : 'bg-[#E7F6EE] text-[#0F7C61]'}`}><Icon name={error ? 'pulse' : 'sparkle'} className="h-5 w-5" /></span><h3 className="mt-5 font-brand text-xl font-bold tracking-[-0.04em] text-[#18252A]">{title}</h3><p className="mt-2 max-w-md text-sm leading-6 text-slate-500">{description}</p>{action && <Link href={action.href} className="e-primary mt-6">{action.label}</Link>}</div>;
}

export function TableSkeleton({ rows = 5, columns = 4 }: { rows?: number; columns?: number }) {
  return <div className="e-panel overflow-hidden"><div className="h-12 bg-slate-50" />{Array.from({ length: rows }).map((_, row) => <div key={row} className="grid gap-5 border-t border-slate-100 px-5 py-5" style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}>{Array.from({ length: columns }).map((__, column) => <span key={column} className="h-3 animate-pulse rounded-full bg-slate-100" />)}</div>)}</div>;
}

export function FieldLabel({ label, hint, required }: { label: string; hint?: string; required?: boolean }) {
  return <span className="mb-2 flex items-center justify-between gap-3 text-sm font-bold text-slate-700"><span>{label}{required && <span className="ml-1 text-red-500">*</span>}</span>{hint && <span className="text-xs font-medium text-slate-400">{hint}</span>}</span>;
}

export function SectionHeader({ eyebrow, title, detail, action }: { eyebrow?: string; title: string; detail?: string; action?: React.ReactNode }) {
  return <div className="flex items-start justify-between gap-4"><div>{eyebrow && <p className="e-eyebrow">{eyebrow}</p>}<h3 className="mt-1 font-brand text-lg font-bold tracking-[-0.04em] text-[#18252A]">{title}</h3>{detail && <p className="mt-1 text-xs leading-5 text-slate-500">{detail}</p>}</div>{action}</div>;
}
