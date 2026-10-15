'use client';

import Link from 'next/link';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Icon } from '../components/AppShell';
import { MetricCard, PageHeader, PrimaryLink, SectionHeader, StageTrack, StatePanel, StatusBadge } from '../components/OperationalUI';
import { ApiError, apiFetch } from '../lib/api';

type Batch = { id: string; species: string; quantityInitial: number; quantityCurrent: number; propagationDate: string; status: string };
type Treatment = { id: string; productName: string; safeShipDate: string | null };
type Diagnostic = { id: string; batchId: string; symptomNotes: string | null; needsReview: boolean; createdAt: string };

const lifecycleStages = ['propagating', 'growing', 'mature', 'sold', 'dead'];

export default function CommandCenter() {
  const [batches, setBatches] = useState<Batch[]>([]);
  const [treatments, setTreatments] = useState<Treatment[]>([]);
  const [diagnostics, setDiagnostics] = useState<Diagnostic[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [batchData, treatmentData, diagnosticData] = await Promise.all([
        apiFetch<Batch[]>('/api/plant-batches'),
        apiFetch<Treatment[]>('/api/treatments'),
        apiFetch<Diagnostic[]>('/api/diagnostics'),
      ]);
      setBatches(batchData ?? []);
      setTreatments(treatmentData ?? []);
      setDiagnostics(diagnosticData ?? []);
    } catch (caught) {
      setBatches([]);
      setTreatments([]);
      setDiagnostics([]);
      setError(caught instanceof ApiError ? caught.message : (caught instanceof Error ? caught.message : 'Live workspace data is temporarily unavailable.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const task = window.setTimeout(() => { void refresh(); }, 0);
    return () => window.clearTimeout(task);
  }, [refresh]);

  const summary = useMemo(() => {
    const activeBatches = batches.filter((batch) => ['propagating', 'growing', 'mature'].includes(batch.status));
    const plantsOnHand = activeBatches.reduce((sum, batch) => sum + batch.quantityCurrent, 0);
    const awaitingReview = diagnostics.filter((item) => item.needsReview).length;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const activeWithholding = treatments.filter((item) => {
      if (!item.safeShipDate) return false;
      return new Date(`${item.safeShipDate}T00:00:00`).getTime() >= today.getTime();
    }).length;
    const stageCounts = lifecycleStages.map((stage) => ({
      stage,
      count: batches.filter((batch) => batch.status === stage).length,
    }));
    return { activeBatches, plantsOnHand, awaitingReview, activeWithholding, stageCounts };
  }, [batches, diagnostics, treatments]);

  const greeting = new Intl.DateTimeFormat('en-US', { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date());
  const pendingDiagnostics = diagnostics.filter((item) => item.needsReview).slice(0, 4);
  const activeTreatments = treatments.filter((item) => item.safeShipDate && new Date(`${item.safeShipDate}T00:00:00`).getTime() >= new Date().setHours(0, 0, 0, 0)).slice(0, 4);

  if (error) {
    return <div className="animate-rise space-y-6"><PageHeader eyebrow={greeting} title="Operations are temporarily unavailable" description="The workspace could not confirm a current data connection, so no estimated inventory or compliance values are shown." action={<button onClick={() => void refresh()} className="e-primary" type="button">Retry connection</button>} /><StatePanel type="error" title="Live workspace data could not be loaded" description={error} /></div>;
  }

  return <div className="animate-rise space-y-6 xl:space-y-7">
    <PageHeader eyebrow={greeting} title="Make the next crop decision count." description="A live operating view across inventory, plant health, treatment records, and customer commitments." action={<div className="flex items-center gap-2"><button onClick={() => void refresh()} className="e-secondary" type="button"><Icon name="command" className="h-4 w-4" />Refresh</button><PrimaryLink href="/batches/new">New crop batch</PrimaryLink></div>} />

    <section className="grid gap-4 md:grid-cols-2 2xl:grid-cols-4" aria-label="Live workspace metrics">
      <MetricCard label="Active crop batches" value={loading ? '—' : summary.activeBatches.length} detail="Propagating, growing, or mature" tone="success" icon="box" />
      <MetricCard label="Plants on hand" value={loading ? '—' : new Intl.NumberFormat('en-US').format(summary.plantsOnHand)} detail="Current quantity in active batches" tone="info" icon="leaf" />
      <MetricCard label="Health reviews" value={loading ? '—' : summary.awaitingReview} detail="Observations awaiting grower review" tone={summary.awaitingReview ? 'attention' : 'success'} icon="pulse" />
      <MetricCard label="Withholding windows" value={loading ? '—' : summary.activeWithholding} detail="Review label and policy before dispatch" tone={summary.activeWithholding ? 'critical' : 'success'} icon="shield" />
    </section>

    <section className="grid gap-5 2xl:grid-cols-[minmax(0,1.5fr)_minmax(330px,0.8fr)]">
      <div className="e-panel overflow-hidden">
        <div className="border-b border-slate-100 px-5 py-5"><SectionHeader eyebrow="Crop inventory" title="Lifecycle distribution" detail="Counts are calculated from current crop batch records." /></div>
        {loading ? <div className="space-y-4 p-5">{lifecycleStages.slice(0, 4).map((stage) => <div key={stage} className="h-5 animate-pulse rounded bg-slate-100" />)}</div> : summary.stageCounts.some((item) => item.count > 0) ? <div className="space-y-4 p-5">{summary.stageCounts.map(({ stage, count }) => {
          const share = batches.length ? Math.round((count / batches.length) * 100) : 0;
          return <div key={stage} className="grid grid-cols-[100px_minmax(0,1fr)_44px] items-center gap-3"><span className="text-xs font-semibold capitalize text-slate-600">{stage}</span><div className="h-2 overflow-hidden rounded-full bg-slate-100"><div className="h-full rounded-full bg-[#0F7C61] transition-[width]" style={{ width: `${share}%` }} /></div><span className="text-right font-data text-xs text-slate-500">{count}</span></div>;
        })}</div> : <div className="p-5"><StatePanel title="No crop batches yet" description="Create the first batch to populate the workspace inventory view." action={{ href: '/batches/new', label: 'Create crop batch' }} /></div>}
      </div>

      <div className="e-panel overflow-hidden"><div className="border-b border-slate-100 px-5 py-5"><SectionHeader eyebrow="Decision queue" title="Needs attention" detail="Only current records from this organization appear here." /></div>
        {loading ? <div className="space-y-3 p-5">{[0, 1, 2].map((item) => <div key={item} className="h-16 animate-pulse rounded-xl bg-slate-100" />)}</div> : pendingDiagnostics.length || activeTreatments.length ? <div className="divide-y divide-slate-100">
          {pendingDiagnostics.map((item) => <Link href="/diagnostics" key={item.id} className="group flex gap-3 px-5 py-4 transition hover:bg-slate-50"><span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-amber-50 text-amber-600"><Icon name="pulse" className="h-[18px] w-[18px]" /></span><span className="min-w-0 flex-1"><span className="block text-sm font-bold leading-5 text-slate-700 group-hover:text-[#0F7C61]">{item.symptomNotes || 'Plant health observation needs review'}</span><span className="mt-1 block text-xs text-slate-400">Batch {item.batchId.slice(0, 8).toUpperCase()} · {new Date(item.createdAt).toLocaleDateString()}</span></span><StatusBadge label="Review" tone="attention" /></Link>)}
          {activeTreatments.map((item) => <Link href="/treatments" key={item.id} className="group flex gap-3 px-5 py-4 transition hover:bg-slate-50"><span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-red-50 text-red-600"><Icon name="shield" className="h-[18px] w-[18px]" /></span><span className="min-w-0 flex-1"><span className="block text-sm font-bold leading-5 text-slate-700 group-hover:text-[#0F7C61]">Treatment control: {item.productName}</span><span className="mt-1 block text-xs text-slate-400">Safe-ship date {item.safeShipDate} · review required</span></span><StatusBadge label="Check" tone="critical" /></Link>)}
        </div> : <div className="p-5"><StatePanel title="No open review items" description="There are no pending diagnostics or active withholding windows in the current records." /></div>}
      </div>
    </section>

    <section className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_340px]">
      <div className="e-panel overflow-hidden"><div className="flex flex-col gap-3 border-b border-slate-100 px-5 py-5 sm:flex-row sm:items-center sm:justify-between"><SectionHeader eyebrow="Crop inventory" title="Priority batches" detail="Current quantity and lifecycle status for recorded crop batches." /><Link href="/batches" className="text-xs font-bold text-[#0F7C61]">Open inventory →</Link></div><div className="overflow-x-auto">{loading ? <div className="space-y-4 p-5">{[0, 1, 2, 3].map((item) => <div key={item} className="h-10 animate-pulse rounded bg-slate-100" />)}</div> : batches.length ? <table className="min-w-[700px] w-full"><thead className="bg-slate-50/70"><tr><th className="e-table-head">Batch</th><th className="e-table-head">Lifecycle</th><th className="e-table-head text-right">Available</th><th className="e-table-head">Propagated</th></tr></thead><tbody className="divide-y divide-slate-100">{batches.slice(0, 5).map((batch) => <tr key={batch.id} className="transition hover:bg-slate-50/70"><td className="e-table-cell"><p className="font-bold text-slate-700">{batch.species}</p><p className="mt-1 font-data text-[10px] text-slate-400">{batch.id.slice(0, 8).toUpperCase()}</p></td><td className="e-table-cell"><StageTrack status={batch.status} /></td><td className="e-table-cell text-right font-data font-bold text-slate-700">{new Intl.NumberFormat('en-US').format(batch.quantityCurrent)}</td><td className="e-table-cell font-data text-xs text-slate-400">{new Date(`${batch.propagationDate}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</td></tr>)}</tbody></table> : <div className="p-5"><StatePanel title="No inventory records" description="Crop batches will appear here after your organization creates them." action={{ href: '/batches/new', label: 'Create crop batch' }} /></div>}</div></div>
      <div className="rounded-2xl bg-[#E7F6EE] p-6"><p className="e-eyebrow text-[#0F7C61]">Operator actions</p><h3 className="mt-2 font-brand text-2xl font-bold tracking-[-0.05em] text-[#16352D]">Keep field work moving.</h3><p className="mt-2 text-sm leading-6 text-[#477467]">Record current work and keep compliance decisions reviewable.</p><div className="mt-6 space-y-2"><QuickLink href="/treatments/new" icon="shield" title="Log application" /><QuickLink href="/diagnostics/new" icon="pulse" title="Capture observation" /><QuickLink href="/orders/new" icon="cart" title="Create customer order" /></div></div>
    </section>
  </div>;
}

function QuickLink({ href, icon, title }: { href: string; icon: Parameters<typeof Icon>[0]['name']; title: string }) {
  return <Link href={href} className="flex items-center gap-3 rounded-xl bg-white/75 px-3 py-3 text-sm font-bold text-[#24483D] transition hover:bg-white"><span className="grid h-8 w-8 place-items-center rounded-lg bg-[#102018] text-[#D9FF5A]"><Icon name={icon} className="h-4 w-4" /></span>{title}<Icon name="arrow" className="ml-auto h-4 w-4 text-[#0F7C61]" /></Link>;
}
