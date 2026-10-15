'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Icon } from '../../components/AppShell';
import { PageHeader, PrimaryLink, StatePanel, StatusBadge } from '../../components/OperationalUI';
import { ApiError, apiFetch, demoModeEnabled } from '../../lib/api';

type Diagnostic = {
  id: string;
  batchId: string;
  symptomNotes: string | null;
  photoUrl?: string | null;
  cvPrediction?: string | null;
  cvConfidence?: number | null;
  needsReview: boolean;
  createdAt: string;
};

const preview: Diagnostic[] = [
  { id: 'DX-204', batchId: 'C-4112', symptomNotes: 'Leaf-edge yellowing across 4 trays; irrigation timing reviewed.', needsReview: true, createdAt: '2026-08-19T08:20:00Z' },
  { id: 'DX-201', batchId: 'C-4109', symptomNotes: 'New growth shows mild spotting after the overnight humidity cycle.', needsReview: true, createdAt: '2026-08-18T14:20:00Z' },
  { id: 'DX-198', batchId: 'C-4124', symptomNotes: 'Stem discoloration on two plants; isolate and inspect before treatment.', needsReview: true, createdAt: '2026-08-17T07:40:00Z' },
];

export default function DiagnosticsPage() {
  const [logs, setLogs] = useState<Diagnostic[]>([]);
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState<'queue' | 'all'>('queue');
  const [previewMode, setPreviewMode] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [reviewingId, setReviewingId] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setLogs((await apiFetch<Diagnostic[]>('/api/diagnostics')) ?? []);
      setPreviewMode(false);
    } catch (caught) {
      if (demoModeEnabled) {
        setLogs(preview);
        setPreviewMode(true);
      } else {
        setLogs([]);
        setPreviewMode(false);
        setError(caught instanceof ApiError ? caught.message : 'Live observations are temporarily unavailable.');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const task = window.setTimeout(() => { void load(); }, 0);
    return () => window.clearTimeout(task);
  }, [load]);

  async function markReviewed(id: string) {
    setReviewingId(id);
    setActionError(null);
    try {
      const reviewed = await apiFetch<Diagnostic>(`/api/diagnostics/${id}/review`, { method: 'POST' });
      setLogs((current) => current.map((entry) => entry.id === id ? reviewed : entry));
    } catch (caught) {
      setActionError(caught instanceof ApiError ? caught.message : 'The review could not be saved.');
    } finally {
      setReviewingId(null);
    }
  }

  const visible = useMemo(() => mode === 'queue' ? logs.filter((entry) => entry.needsReview) : logs, [logs, mode]);
  const reviews = logs.filter((entry) => entry.needsReview).length;

  if (error) {
    return (
      <div className="animate-rise space-y-6">
        <PageHeader eyebrow="Plant health intelligence" title="Review queue unavailable" description="The workspace could not confirm current observation records." action={<button onClick={() => void load()} className="e-primary" type="button">Retry connection</button>} />
        <StatePanel type="error" title="Live observations could not be loaded" description={error} />
      </div>
    );
  }

  return (
    <div className="animate-rise space-y-6">
      <PageHeader eyebrow="Plant health intelligence" title="Review queue" description="Prioritize observations, preserve field context, and keep every plant-health decision traceable." action={<PrimaryLink href="/diagnostics/new" icon="pulse">Capture observation</PrimaryLink>} />
      <section className="grid gap-4 md:grid-cols-3">
        <HealthStat label="Awaiting review" value={`${reviews}`} detail="records require a grower decision" icon="pulse" tone="attention" />
        <HealthStat label="Total observations" value={`${logs.length}`} detail="records in the current organization" icon="command" tone="info" />
        <HealthStat label="Review policy" value="Required" detail="advisory output never replaces human review" icon="shield" tone="success" />
      </section>
      {previewMode && <div className="rounded-xl border border-cyan-100 bg-cyan-50 px-4 py-3 text-xs text-cyan-800"><strong>Preview mode:</strong> representative observations are shown until live services reconnect.</div>}
      {actionError && <div role="alert" className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">{actionError}</div>}
      <section className="e-panel overflow-hidden">
        <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex rounded-xl bg-slate-100 p-1 text-xs font-bold">
            <button onClick={() => setMode('queue')} className={`rounded-lg px-3 py-1.5 ${mode === 'queue' ? 'bg-white text-slate-700 shadow-sm' : 'text-slate-400'}`}>Needs review ({reviews})</button>
            <button onClick={() => setMode('all')} className={`rounded-lg px-3 py-1.5 ${mode === 'all' ? 'bg-white text-slate-700 shadow-sm' : 'text-slate-400'}`}>All observations</button>
          </div>
          <button onClick={() => void load()} className="text-xs font-bold text-[#0F7C61]" type="button">Refresh queue</button>
        </div>
        {loading ? <div className="space-y-3 p-5">{Array.from({ length: 4 }).map((_, index) => <div key={index} className="h-28 animate-pulse rounded-xl bg-slate-100" />)}</div> : visible.length ? (
          <div className="divide-y divide-slate-100">
            {visible.map((log) => (
              <article key={log.id} className="flex gap-4 px-5 py-5 transition hover:bg-slate-50/80">
                <span className={`grid h-11 w-11 shrink-0 place-items-center rounded-2xl ${log.needsReview ? 'bg-amber-50 text-amber-600' : 'bg-emerald-50 text-emerald-600'}`}><Icon name="pulse" className="h-5 w-5" /></span>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <p className="text-sm font-bold leading-5 text-slate-700">{log.symptomNotes || 'Field observation recorded without notes'}</p>
                      <p className="mt-1.5 font-data text-[10px] text-slate-400">{log.id} · BATCH {log.batchId?.slice(0, 8).toUpperCase()} · {new Date(log.createdAt).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })}</p>
                    </div>
                    <StatusBadge label={log.needsReview ? 'Decision required' : 'Reviewed'} tone={log.needsReview ? 'attention' : 'success'} />
                  </div>
                  {log.cvPrediction && <div className="mt-3 flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs text-slate-500"><Icon name="sparkle" className="h-3.5 w-3.5 text-[#0F7C61]" /><span><strong className="text-slate-700">Advisory signal:</strong> {log.cvPrediction}{typeof log.cvConfidence === 'number' ? ` · ${Math.round(log.cvConfidence * 100)}% confidence` : ''}</span></div>}
                  {log.needsReview && <div className="mt-4"><button type="button" disabled={previewMode || reviewingId === log.id} onClick={() => void markReviewed(log.id)} className="e-secondary disabled:cursor-not-allowed disabled:opacity-50">{reviewingId === log.id ? 'Saving review…' : 'Mark reviewed'}</button></div>}
                </div>
              </article>
            ))}
          </div>
        ) : <div className="p-5"><StatePanel title={mode === 'queue' ? 'No observations need review' : 'No observations yet'} description={mode === 'queue' ? 'The current plant-health queue is clear. New field captures will appear here.' : 'Capture a field observation to create the first review record.'} action={{ href: '/diagnostics/new', label: 'Capture observation' }} /></div>}
      </section>
    </div>
  );
}

function HealthStat({ label, value, detail, icon, tone }: { label: string; value: string; detail: string; icon: Parameters<typeof Icon>[0]['name']; tone: 'success' | 'attention' | 'info' }) {
  return <div className="e-panel-flat flex items-center gap-4 p-4"><span className="grid h-10 w-10 place-items-center rounded-xl bg-slate-100 text-[#0F7C61]"><Icon name={icon} className="h-5 w-5" /></span><span><span className="e-eyebrow block">{label}</span><span className="mt-1 block font-brand text-xl font-bold tracking-[-0.05em] text-[#18252A]">{value}</span><span className={`mt-0.5 block text-[11px] ${tone === 'attention' ? 'text-amber-600' : tone === 'success' ? 'text-emerald-600' : 'text-slate-400'}`}>{detail}</span></span></div>;
}
