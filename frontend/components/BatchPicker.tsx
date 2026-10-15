'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { ApiError, apiFetch } from '../lib/api';
import { FieldLabel } from './OperationalUI';

type BatchOption = {
  id: string;
  species: string;
  quantityCurrent: number;
  status: string;
};

export function BatchPicker() {
  const [batches, setBatches] = useState<BatchOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchBatches = useCallback(() => apiFetch<BatchOption[]>('/api/plant-batches'), []);

  useEffect(() => {
    let mounted = true;
    fetchBatches()
      .then((result) => {
        if (!mounted) return;
        setBatches(result ?? []);
        setError(null);
      })
      .catch((caught: unknown) => {
        if (!mounted) return;
        setError(caught instanceof ApiError ? caught.message : 'Crop batches could not be loaded.');
        setBatches([]);
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => { mounted = false; };
  }, [fetchBatches]);

  function retryLoad() {
    setLoading(true);
    setError(null);
    fetchBatches()
      .then((result) => setBatches(result ?? []))
      .catch((caught: unknown) => {
        setError(caught instanceof ApiError ? caught.message : 'Crop batches could not be loaded.');
        setBatches([]);
      })
      .finally(() => setLoading(false));
  }

  return (
    <div>
      <FieldLabel label="Crop batch" hint="Select a batch in this workspace" required />
      <select className="e-select font-data" id="batchId" name="batchId" required aria-describedby="batch-picker-help">
        <option value="" disabled>{loading ? 'Loading crop batches…' : 'Select a crop batch'}</option>
        {batches.map((batch) => (
          <option key={batch.id} value={batch.id}>
            {batch.species} · {batch.id.slice(0, 8).toUpperCase()} · {batch.quantityCurrent} available · {batch.status}
          </option>
        ))}
      </select>
      <div id="batch-picker-help" className="mt-2 text-xs text-slate-500">
        {loading && <p>Loading batches from this organization…</p>}
        {!loading && error && (
          <div role="alert" className="flex flex-wrap items-center gap-2 text-red-700">
            <span>{error}</span>
            <button type="button" onClick={retryLoad} className="font-bold underline underline-offset-2">Try again</button>
          </div>
        )}
        {!loading && !error && batches.length === 0 && (
          <p>No crop batches are available. <Link className="font-bold text-[#0F7C61] underline underline-offset-2" href="/batches/new">Create a batch</Link> first.</p>
        )}
      </div>
    </div>
  );
}
