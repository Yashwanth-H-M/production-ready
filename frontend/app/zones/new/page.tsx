'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { FieldLabel, PageHeader } from '../../../components/OperationalUI';
import { ApiError, apiFetch } from '../../../lib/api';

export default function NewZonePage() {
  const router = useRouter();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    const form = new FormData(event.currentTarget);
    const profile: Record<string, number> = {};
    const fields = [
      ['tempMin', 'temp_min'],
      ['tempMax', 'temp_max'],
      ['humidityTarget', 'humidity_target'],
    ] as const;

    for (const [field, key] of fields) {
      const raw = String(form.get(field) ?? '').trim();
      if (!raw) continue;
      const value = Number(raw);
      if (!Number.isFinite(value)) {
        setError('Environmental targets must be valid numbers.');
        setSaving(false);
        return;
      }
      profile[key] = value;
    }

    if (profile.temp_min != null && profile.temp_max != null && profile.temp_min > profile.temp_max) {
      setError('Minimum temperature must not exceed maximum temperature.');
      setSaving(false);
      return;
    }
    if (profile.humidity_target != null && (profile.humidity_target < 0 || profile.humidity_target > 100)) {
      setError('Humidity target must be between 0 and 100 percent.');
      setSaving(false);
      return;
    }

    try {
      await apiFetch('/api/greenhouse-zones', {
        method: 'POST',
        body: JSON.stringify({ name: String(form.get('name') ?? '').trim(), microclimate: profile }),
      });
      router.push('/zones');
      router.refresh();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : 'The growing zone could not be created.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="animate-rise mx-auto max-w-4xl space-y-6">
      <PageHeader eyebrow="Operating network / New zone" title="Create growing zone" description="Record a physical growing environment and its optional environmental targets." trail="Sites & zones" />
      <form onSubmit={submit} className="e-panel overflow-hidden">
        <div className="grid gap-6 p-6 sm:grid-cols-2 sm:p-8">
          <div className="sm:col-span-2">
            <FieldLabel label="Zone name" hint="Use a name operators recognize in the field" required />
            <input className="e-input" id="zoneName" name="name" required maxLength={120} placeholder="e.g. Propagation House A" />
          </div>
          <div className="sm:col-span-2"><h3 className="font-brand text-lg font-bold text-[#18252A]">Environmental targets</h3><p className="mt-1 text-xs text-slate-500">Optional reference targets; live sensor monitoring is not connected.</p></div>
          <div>
            <FieldLabel label="Minimum temperature" hint="°C; optional" />
            <input className="e-input" name="tempMin" type="number" step="0.1" placeholder="20" />
          </div>
          <div>
            <FieldLabel label="Maximum temperature" hint="°C; optional" />
            <input className="e-input" name="tempMax" type="number" step="0.1" placeholder="24" />
          </div>
          <div>
            <FieldLabel label="Humidity target" hint="Percent; optional" />
            <input className="e-input" name="humidityTarget" type="number" min="0" max="100" step="1" placeholder="70" />
          </div>
          {error && <div role="alert" className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700 sm:col-span-2">{error}</div>}
        </div>
        <div className="flex flex-col-reverse gap-3 border-t border-slate-100 bg-slate-50/70 px-6 py-4 sm:flex-row sm:justify-end sm:px-8">
          <Link href="/zones" className="e-secondary">Cancel</Link>
          <button disabled={saving} type="submit" className="e-primary">{saving ? 'Creating zone…' : 'Create growing zone'}</button>
        </div>
      </form>
    </div>
  );
}
