'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Icon } from '../../components/AppShell';
import { PageHeader, PrimaryLink, StatePanel, StatusBadge, TableSkeleton } from '../../components/OperationalUI';
import { ApiError, apiFetch, demoModeEnabled } from '../../lib/api';

type Treatment = { id: string; productName: string; treatmentType: string; appliedDate: string; applicationRate?: string | null; withholdingDays: number; safeShipDate: string | null };

const preview: Treatment[] = [
  { id: 'TR-092', productName: 'Pyrethrin Spray', treatmentType: 'chemical', appliedDate: '2026-08-12', applicationRate: '1 oz / gal', withholdingDays: 14, safeShipDate: '2026-08-26' },
  { id: 'TR-089', productName: 'Neem Oil Extract', treatmentType: 'organic', appliedDate: '2026-08-18', applicationRate: '2 tbsp / gal', withholdingDays: 0, safeShipDate: '2026-08-18' },
  { id: 'TR-084', productName: 'Biofungicide', treatmentType: 'organic', appliedDate: '2026-08-05', applicationRate: 'Label rate', withholdingDays: 0, safeShipDate: '2026-08-05' },
];

function shipmentState(date: string | null) {
  if (!date) return { label: 'Needs review', tone: 'attention' as const, detail: 'No safe-ship date' };
  const day = new Date(`${date}T00:00:00`);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const remaining = Math.ceil((day.getTime() - today.getTime()) / 86400000);
  return remaining >= 0
    ? { label: remaining ? `Withholding · ${remaining}d` : 'Withholding today', tone: 'critical' as const, detail: `Calculated date ${day.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}` }
    : { label: 'Manual sign-off', tone: 'attention' as const, detail: `Calculated date passed ${day.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}` };
}

export default function TreatmentsPage() {
  const [items, setItems] = useState<Treatment[]>([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<'all' | 'active'>('all');
  const [previewMode, setPreviewMode] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setItems((await apiFetch<Treatment[]>('/api/treatments')) ?? []);
      setPreviewMode(false);
    } catch (caught) {
      if (demoModeEnabled) {
        setItems(preview);
        setPreviewMode(true);
      } else {
        setItems([]);
        setPreviewMode(false);
        setError(caught instanceof ApiError ? caught.message : 'Live treatment records are temporarily unavailable.');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const task = window.setTimeout(() => { void load(); }, 0);
    return () => window.clearTimeout(task);
  }, [load]);

  const active = items.filter((item) => shipmentState(item.safeShipDate).tone === 'critical').length;
  const visible = useMemo(() => view === 'active' ? items.filter((item) => shipmentState(item.safeShipDate).tone === 'critical') : items, [items, view]);

  if (error) {
    return (
      <div className="animate-rise space-y-6">
        <PageHeader eyebrow="Compliance operations" title="Application register unavailable" description="The workspace could not confirm current treatment records." action={<button onClick={() => void load()} className="e-primary" type="button">Retry connection</button>} />
        <StatePanel type="error" title="Live treatment records could not be loaded" description={error} />
      </div>
    );
  }

  return (
    <div className="animate-rise space-y-6">
      <PageHeader eyebrow="Compliance operations" title="Application register" description="Review recorded crop applications and their calculated withholding dates. Human review against the product label remains required." action={<PrimaryLink href="/treatments/new" icon="shield">Log application</PrimaryLink>} />
      <section className="grid gap-4 lg:grid-cols-[1.3fr_1fr_1fr]">
        <ControlCard icon="shield" label="Calculated withholding windows" value={`${active}`} detail="Dates today or later; confirm the product label" tone={active ? 'critical' : 'info'} />
        <ControlCard icon="pulse" label="Total applications" value={`${items.length}`} detail="All application records in this workspace" tone="info" />
        <ControlCard icon="sparkle" label="Compliance posture" value="Review-led" detail="No calculated date authorizes shipment" tone="attention" />
      </section>
      {previewMode && <div className="rounded-xl border border-cyan-100 bg-cyan-50 px-4 py-3 text-xs text-cyan-800"><strong>Preview mode:</strong> representative compliance records are shown until live services reconnect.</div>}
      <section className="e-panel overflow-hidden">
        <div className="flex items-center justify-between border-b border-slate-100 p-4">
          <div className="flex rounded-xl bg-slate-100 p-1 text-xs font-bold">
            <button onClick={() => setView('all')} className={`rounded-lg px-3 py-1.5 ${view === 'all' ? 'bg-white text-slate-700 shadow-sm' : 'text-slate-400'}`}>All applications</button>
            <button onClick={() => setView('active')} className={`rounded-lg px-3 py-1.5 ${view === 'active' ? 'bg-white text-slate-700 shadow-sm' : 'text-slate-400'}`}>Calculated windows {active ? `(${active})` : ''}</button>
          </div>
          <button onClick={() => void load()} className="text-xs font-bold text-[#0F7C61]" type="button">Refresh register</button>
        </div>
        {loading ? <TableSkeleton rows={5} columns={5} /> : visible.length ? (
          <div className="overflow-x-auto">
            <table className="min-w-[860px] w-full">
              <thead className="bg-slate-50/70"><tr><th className="e-table-head">Application</th><th className="e-table-head">Category</th><th className="e-table-head">Applied</th><th className="e-table-head">Withholding</th><th className="e-table-head">Calculated control</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {visible.map((item) => {
                  const shipment = shipmentState(item.safeShipDate);
                  return <tr key={item.id} className="transition hover:bg-slate-50/70">
                    <td className="e-table-cell"><p className="font-bold text-slate-700">{item.productName}</p><p className="mt-1 text-[11px] text-slate-400">{item.applicationRate || 'Rate not recorded'}</p></td>
                    <td className="e-table-cell"><StatusBadge label={item.treatmentType} tone={item.treatmentType === 'chemical' ? 'attention' : 'success'} /></td>
                    <td className="e-table-cell font-data text-xs text-slate-500">{new Date(`${item.appliedDate}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</td>
                    <td className="e-table-cell"><span className="font-data font-bold text-slate-700">{item.withholdingDays}d</span></td>
                    <td className="e-table-cell"><StatusBadge label={shipment.label} tone={shipment.tone} /><p className="mt-1.5 text-[11px] text-slate-400">{shipment.detail}</p></td>
                  </tr>;
                })}
              </tbody>
            </table>
          </div>
        ) : <div className="p-5"><StatePanel title={view === 'active' ? 'No calculated withholding windows' : 'No applications recorded'} description={view === 'active' ? 'No calculated date is today or later. Verify product labels and policy before shipment.' : 'Log a field application to add the first controlled record.'} action={{ href: '/treatments/new', label: 'Log application' }} /></div>}
      </section>
    </div>
  );
}

function ControlCard({ icon, label, value, detail, tone }: { icon: Parameters<typeof Icon>[0]['name']; label: string; value: string; detail: string; tone: 'success' | 'attention' | 'critical' | 'info' }) {
  const map = { success: 'bg-emerald-50 text-emerald-600', attention: 'bg-amber-50 text-amber-600', critical: 'bg-red-50 text-red-600', info: 'bg-cyan-50 text-cyan-600' };
  return <div className="e-panel-flat flex items-center gap-4 p-4"><span className={`grid h-10 w-10 place-items-center rounded-xl ${map[tone]}`}><Icon name={icon} className="h-5 w-5" /></span><span><span className="e-eyebrow block">{label}</span><span className="mt-1 block font-brand text-xl font-bold tracking-[-.05em] text-[#18252A]">{value}</span><span className="mt-0.5 block text-[11px] text-slate-400">{detail}</span></span></div>;
}
