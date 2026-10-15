---
name: offline-field-sync
description: Use this skill when building any feature where a greenhouse worker records data (treatments, diagnostics, batch moves, mortality) that must work with no network connectivity. Covers the append-only event pattern that makes offline writes safe to sync later without silently losing or overwriting data.
---

# Offline field-event sync

## The problem this solves

IndexedDB read-caching solves "the worker can see data with no signal."
It does not solve "the worker logs a treatment offline while someone in
the office marks that same batch sold in the meantime." Overwrite-style
sync (last-write-wins on a mutable row) silently drops one of the two
changes. For inventory that represents real, physical, sometimes legally
regulated actions (treatments, mortality counts), silent data loss isn't
acceptable.

## The pattern to use instead

Every field action is an immutable, append-only row in `batch_events`
(see `database/schema.sql`), never a direct update to `plant_batches` or
any other mutable table. The current state of a batch is a *projection*
computed from its event history, not a value the client writes directly.

Client side (`frontend/lib/offlineSync.ts`): actions are queued locally
in IndexedDB with a client-generated UUID (for idempotent replay) and a
client-captured timestamp, then replayed against the server in order once
connectivity returns.

```typescript
interface FieldEvent {
  id: string;            // client-generated UUID — replay key
  batchId: string;
  type: 'TREATMENT_LOGGED' | 'DIAGNOSTIC_LOGGED' | 'BATCH_MOVED' | 'MORTALITY_RECORDED';
  payload: Record<string, unknown>;
  occurredAt: string;     // captured on-device, may predate server receipt
  synced: boolean;
}
```

Server side: the sync endpoint inserts each event by its client-generated
`id` (so replaying the same event twice is a no-op, not a duplicate), and
never mutates an existing `batch_events` row.

## Checklist for any new field-recordable action

- [ ] The action is modeled as a new `event_type`, not a new mutable
      column somewhere.
- [ ] The client queues it via `queueFieldEvent(...)` rather than trying
      to update local cached state directly.
- [ ] The server-side sync handler is idempotent on the client-generated
      event id (replay-safe).
- [ ] If two events for the same batch could genuinely conflict (e.g. two
      different mortality counts logged offline by two different
      people), that surfaces as something a human resolves — don't pick
      a winner silently. Phase 2 needs an actual conflict-review UI for
      this; until it exists, don't ship a feature that can produce a
      silent conflict.

## What not to do

- Don't let any offline-capable feature write directly to a mutable
  table's current-state columns from the client. If it's recorded in the
  field, it's an event first.
- Don't design a "last sync wins" merge for anything that represents a
  real-world physical fact (mortality, treatment applied, batch moved).
  That's data loss wearing a UX feature.
