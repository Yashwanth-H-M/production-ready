'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { ApiError, apiFetch } from '../lib/api';
import { FieldLabel } from './OperationalUI';

type ZoneOption = { id: string; name: string };

export function ZonePicker() {
  const [zones, setZones] = useState<ZoneOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    apiFetch<ZoneOption[]>('/api/greenhouse-zones')
      .then((result) => {
        if (mounted) setZones(result ?? []);
      })
      .catch((caught: unknown) => {
        if (mounted) setError(caught instanceof ApiError ? caught.message : 'Growing zones could not be loaded.');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => { mounted = false; };
  }, []);

  return (
    <div className="sm:col-span-2">
      <FieldLabel label="Growing zone" hint="Optional" />
      <select className="e-select" name="zoneId" defaultValue="">
        <option value="">Leave unassigned</option>
        {zones.map((zone) => <option key={zone.id} value={zone.id}>{zone.name}</option>)}
      </select>
      {loading && <p className="mt-2 text-xs text-slate-400">Loading workspace zones…</p>}
      {!loading && error && <p className="mt-2 text-xs text-amber-700">{error} This batch can still be created without a zone.</p>}
      {!loading && !error && zones.length === 0 && <p className="mt-2 text-xs text-slate-500">No zones configured. <Link className="font-bold text-[#0F7C61] underline underline-offset-2" href="/zones/new">Create a growing zone</Link>.</p>}
    </div>
  );
}
