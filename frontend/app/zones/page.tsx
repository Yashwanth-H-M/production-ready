'use client';

import { useCallback, useEffect, useState } from 'react';
import { Icon } from '../../components/AppShell';
import { PageHeader, PrimaryLink, StatePanel, StatusBadge } from '../../components/OperationalUI';
import { ApiError, apiFetch, demoModeEnabled } from '../../lib/api';

type Zone = { id: string; name: string; microclimate?: Record<string, number> | null };

const preview: Zone[] = [
  { id: 'GH-A', name: 'Propagation House A', microclimate: { temp_min: 20, temp_max: 24, humidity_target: 75 } },
  { id: 'GH-B', name: 'Growing House B', microclimate: { temp_min: 18, temp_max: 26, humidity_target: 65 } },
  { id: 'GH-C', name: 'Mature Sales Floor', microclimate: { temp_min: 15, temp_max: 22, humidity_target: 55 } },
];

function description(microclimate?: Zone['microclimate']) {
  if (!microclimate) return 'Environmental targets have not been defined.';
  const temp = microclimate.temp_min != null && microclimate.temp_max != null ? `${microclimate.temp_min}–${microclimate.temp_max}°C` : null;
  const humidity = microclimate.humidity_target != null ? `${microclimate.humidity_target}% humidity` : null;
  return [temp, humidity].filter(Boolean).join(' · ') || 'Environmental targets have not been defined.';
}

export default function ZonesPage() {
  const [zones, setZones] = useState<Zone[]>([]);
  const [loading, setLoading] = useState(true);
  const [previewMode, setPreviewMode] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setZones((await apiFetch<Zone[]>('/api/greenhouse-zones')) ?? []);
      setPreviewMode(false);
    } catch (caught) {
      if (demoModeEnabled) {
        setZones(preview);
        setPreviewMode(true);
      } else {
        setZones([]);
        setPreviewMode(false);
        setError(caught instanceof ApiError ? caught.message : 'Live growing zones are temporarily unavailable.');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const task = window.setTimeout(() => { void load(); }, 0);
    return () => window.clearTimeout(task);
  }, [load]);

  if (error) {
    return (
      <div className="animate-rise space-y-6">
        <PageHeader eyebrow="Operating network" title="Growing zones unavailable" description="The workspace could not confirm its current zone records." action={<button onClick={() => void load()} className="e-primary" type="button">Retry connection</button>} />
        <StatePanel type="error" title="Live zones could not be loaded" description={error} />
      </div>
    );
  }

  return (
    <div className="animate-rise space-y-6">
      <PageHeader eyebrow="Operating network" title="Sites & zones" description="Define physical growing environments and record their operating targets." action={<PrimaryLink href="/zones/new" icon="map">Create growing zone</PrimaryLink>} />
      {previewMode && <div className="rounded-xl border border-cyan-100 bg-cyan-50 px-4 py-3 text-xs text-cyan-800"><strong>Preview mode:</strong> representative site information is shown until live services reconnect.</div>}
      <section className="rounded-2xl bg-[#102018] p-6 text-white">
        <div className="grid gap-6 md:grid-cols-[1fr_auto] md:items-end">
          <div>
            <p className="text-[10px] font-bold uppercase tracking-[.16em] text-white/45">Network overview</p>
            <h3 className="mt-2 font-brand text-[30px] font-bold tracking-[-.06em] text-white">One operating picture across every growing environment.</h3>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-white/55">Zone records anchor crop inventory and provide a foundation for future sensor-driven alerting.</p>
          </div>
          <div className="flex gap-8">
            <Metric label="Zones" value={`${zones.length}`.padStart(2, '0')} />
            <Metric label="Sensor feeds" value="Planned" />
          </div>
        </div>
      </section>
      {loading ? <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{Array.from({ length: 3 }).map((_, index) => <div key={index} className="h-56 animate-pulse rounded-2xl bg-slate-100" />)}</div> : zones.length ? (
        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {zones.map((zone) => (
            <article key={zone.id} className="e-panel overflow-hidden p-5 transition hover:-translate-y-1 hover:shadow-[0_16px_30px_rgba(23,36,39,0.08)]">
              <div className="flex items-start justify-between">
                <span className="grid h-11 w-11 place-items-center rounded-2xl bg-[#E7F6EE] text-[#0F7C61]"><Icon name="map" className="h-5 w-5" /></span>
                <span className="font-data text-[10px] font-bold text-slate-400">{zone.id.slice(0, 8).toUpperCase()}</span>
              </div>
              <h3 className="mt-6 font-brand text-xl font-bold tracking-[-.045em] text-[#18252A]">{zone.name}</h3>
              <p className="mt-2 min-h-10 text-sm leading-5 text-slate-500">{description(zone.microclimate)}</p>
              <div className="mt-6 flex items-center justify-between border-t border-slate-100 pt-4">
                <StatusBadge label="Zone configured" tone="info" />
                <span className="text-xs text-slate-400">Sensor telemetry planned</span>
              </div>
            </article>
          ))}
        </section>
      ) : <StatePanel title="No operating zones configured" description="Create a growing zone to define physical environments and environmental targets." action={{ href: '/zones/new', label: 'Create growing zone' }} />}
      <div className="flex justify-end"><button onClick={() => void load()} className="text-xs font-bold text-[#0F7C61]" type="button">Refresh network</button></div>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return <div><p className="text-[10px] font-bold uppercase tracking-[.16em] text-white/45">{label}</p><p className="mt-2 font-data text-2xl font-bold text-white">{value}</p></div>;
}
