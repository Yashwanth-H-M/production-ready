'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Icon } from '../../components/AppShell';
import { MetricCard, PageHeader, PrimaryLink, StatePanel, StatusBadge, TableSkeleton } from '../../components/OperationalUI';
import { ApiError, apiFetch, demoModeEnabled } from '../../lib/api';

type Order = { id: string; customerName: string; orderType: string; status: string; createdAt: string };

const preview: Order[] = [
  { id: 'SO-1849', customerName: 'City Garden Center', orderType: 'standard', status: 'pending', createdAt: '2026-08-19T09:10:00Z' },
  { id: 'SO-1844', customerName: 'Landscape Pros LLC', orderType: 'wholesale', status: 'pending', createdAt: '2026-08-17T12:10:00Z' },
  { id: 'SO-1838', customerName: 'Northside Living', orderType: 'contract_grow', status: 'allocated', createdAt: '2026-08-14T10:10:00Z' },
];

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [previewMode, setPreviewMode] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setOrders((await apiFetch<Order[]>('/api/orders')) ?? []);
      setPreviewMode(false);
    } catch (caught) {
      if (demoModeEnabled) {
        setOrders(preview);
        setPreviewMode(true);
      } else {
        setOrders([]);
        setPreviewMode(false);
        setError(caught instanceof ApiError ? caught.message : 'Live orders are temporarily unavailable.');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const task = window.setTimeout(() => { void load(); }, 0);
    return () => window.clearTimeout(task);
  }, [load]);

  const visible = useMemo(
    () => orders.filter((order) => !query || `${order.customerName} ${order.id}`.toLowerCase().includes(query.toLowerCase())),
    [orders, query],
  );
  const pending = orders.filter((order) => order.status === 'pending').length;
  const customerCount = new Set(orders.map((order) => order.customerName.trim().toLocaleLowerCase()).filter(Boolean)).size;

  if (error) {
    return (
      <div className="animate-rise space-y-6">
        <PageHeader eyebrow="Commercial operations" title="Orders unavailable" description="The workspace could not confirm current customer commitments." action={<button onClick={() => void load()} className="e-primary" type="button">Retry connection</button>} />
        <StatePanel type="error" title="Live orders could not be loaded" description={error} />
      </div>
    );
  }

  return (
    <div className="animate-rise space-y-6">
      <PageHeader eyebrow="Commercial operations" title="Revenue pipeline" description="Review customer commitments and their current workflow state." action={<PrimaryLink href="/orders/new" icon="cart">Create order</PrimaryLink>} />
      <section className="grid gap-4 md:grid-cols-3">
        <MetricCard label="Open commitments" value={orders.length} detail="customer orders in this workspace" icon="cart" tone="info" />
        <MetricCard label="Needs allocation" value={pending} detail="orders awaiting inventory decisions" icon="box" tone={pending ? 'attention' : 'success'} />
        <MetricCard label="Customers" value={customerCount} detail="represented by recorded orders" icon="sparkle" tone="neutral" />
      </section>
      {previewMode && <div className="rounded-xl border border-cyan-100 bg-cyan-50 px-4 py-3 text-xs text-cyan-800"><strong>Preview mode:</strong> representative orders are shown until live services reconnect.</div>}
      <section className="e-panel overflow-hidden">
        <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="relative w-full sm:max-w-sm">
            <Icon name="search" className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input className="e-input pl-10" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search customer or order ID" />
          </div>
          <button onClick={() => void load()} className="text-xs font-bold text-[#0F7C61]" type="button">Refresh pipeline</button>
        </div>
        {loading ? <TableSkeleton rows={5} columns={4} /> : visible.length ? (
          <div className="overflow-x-auto">
            <table className="min-w-[700px] w-full">
              <thead className="bg-slate-50/70"><tr><th className="e-table-head">Customer</th><th className="e-table-head">Order type</th><th className="e-table-head">Workflow state</th><th className="e-table-head">Created</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {visible.map((order) => (
                  <tr key={order.id} className="transition hover:bg-slate-50/80">
                    <td className="e-table-cell"><p className="font-bold text-slate-700">{order.customerName}</p><p className="mt-1 font-data text-[10px] text-slate-400">{order.id.slice(0, 8).toUpperCase()}</p></td>
                    <td className="e-table-cell capitalize">{order.orderType.replace('_', ' ')}</td>
                    <td className="e-table-cell"><StatusBadge label={order.status === 'pending' ? 'Awaiting allocation' : order.status} tone={order.status === 'pending' ? 'attention' : 'success'} /></td>
                    <td className="e-table-cell font-data text-xs text-slate-400">{new Date(order.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <div className="p-5"><StatePanel title="No orders match this search" description="Try another customer name or create a new commercial commitment." action={{ href: '/orders/new', label: 'Create order' }} /></div>}
      </section>
    </div>
  );
}
