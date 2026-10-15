/**
 * Offline field-event sync queue.
 * Phase 1: stub — read cache only. Phase 2 will implement full write queue.
 */

export interface FieldEvent {
  id: string;
  batchId: string;
  type: 'TREATMENT_LOGGED' | 'DIAGNOSTIC_LOGGED' | 'BATCH_MOVED' | 'MORTALITY_RECORDED';
  payload: Record<string, unknown>;
  occurredAt: string;
  synced: boolean;
}

const DB_NAME = 'nursery-crm';
const DB_VERSION = 1;

function openDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onerror = () => reject(req.error);
    req.onsuccess = () => resolve(req.result);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains('events')) {
        db.createObjectStore('events', { keyPath: 'id' });
      }
      if (!db.objectStoreNames.contains('cache')) {
        db.createObjectStore('cache', { keyPath: 'key' });
      }
    };
  });
}

export async function queueFieldEvent(event: FieldEvent): Promise<void> {
  const db = await openDB();
  const tx = db.transaction('events', 'readwrite');
  tx.objectStore('events').put({ ...event, synced: false });
  return new Promise((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export async function getQueuedEvents(): Promise<FieldEvent[]> {
  const db = await openDB();
  const tx = db.transaction('events', 'readonly');
  const store = tx.objectStore('events');
  return new Promise((resolve, reject) => {
    const req = store.getAll();
    req.onsuccess = () => resolve(req.result as FieldEvent[]);
    req.onerror = () => reject(req.error);
  });
}

export async function setCache(key: string, value: unknown): Promise<void> {
  const db = await openDB();
  const tx = db.transaction('cache', 'readwrite');
  tx.objectStore('cache').put({ key, value, cachedAt: Date.now() });
  return new Promise((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export async function getCache(key: string): Promise<unknown | null> {
  const db = await openDB();
  const tx = db.transaction('cache', 'readonly');
  const store = tx.objectStore('cache');
  return new Promise((resolve, reject) => {
    const req = store.get(key);
    req.onsuccess = () => resolve(req.result?.value ?? null);
    req.onerror = () => reject(req.error);
  });
}
