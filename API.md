# API Reference

## Overview

The Spring Boot backend exposes JSON REST endpoints under `/api`. The Next.js frontend calls these endpoints through its same-origin `/api/*` gateway route. Production clients must present a verified bearer token; tenant ownership is derived from token claims and must never be supplied as an application-level field.

> **Do not send `tenantId` or `X-Tenant-ID` as a way to select a workspace.** The backend derives tenant identity from a verified authenticated principal and rejects a conflicting tenant header.

## Conventions

| Concern | Convention |
|---|---|
| Base path | `/api` |
| Media type | `application/json` |
| Authentication | `Authorization: Bearer <JWT>` for protected endpoints |
| IDs | UUIDs unless a response also exposes a business-facing display identifier |
| Timestamps | ISO-8601 strings; batch events use UTC instants |
| Validation | Invalid request bodies produce a client error with an actionable message |
| Unknown record | Single-record lookup returns `404 Not Found` |
| Service outage | The frontend gateway returns a controlled `503` response if the backend is unavailable |

## Authentication

### Local demonstration session

`POST /api/auth/local-session` is a local-development convenience only. It takes no request body and, when `LOCAL_DEMO_AUTH_ENABLED=true`, returns a signed HS256 token for the configured seeded user and tenant. The production Compose profile sets the switch to `false`.

Production uses Clerk Organizations. The frontend gateway forwards a signed Clerk session token; the backend validates its RS256 signature, issuer, expiry, and `canopy-api` audience. Configure the default session token with custom `aud`, `email`, and `org_name` claims. The backend requires `sub`, `email`, `org_name`, and active organization membership claims: `org_id`/`org_role` for Clerk token v1 or nested `o.id`/`o.rol` for v2. The repository does not expose a production password or self-registration flow.

### Token claims

The Clerk JWT converter accepts only verified claims, derives stable internal UUIDs from the Clerk issuer, organization, and subject, and creates a `TenantPrincipal`. The tenant filter provisions the organization directory entry and user record through the tenant-aware executor. Never accept `tenantId` from request JSON or a tenant-selection header.

## Health endpoint

| Method | Path | Purpose | Auth |
|---|---|---|---|
| `GET` | `/health` | Lightweight backend liveness response (`ok`) | No |

## Crop inventory

Base path: `/api/plant-batches`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/plant-batches` | Create a crop batch. |
| `GET` | `/api/plant-batches` | List crop batches visible to the verified tenant. |
| `GET` | `/api/plant-batches/{id}` | Retrieve one crop batch. |
| `PUT` | `/api/plant-batches/{id}` | Update a crop batch's current state. |
| `DELETE` | `/api/plant-batches/{id}` | Delete a crop batch. |

### Crop batch request fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `species` | string | Yes | Commercial crop name. |
| `zoneId` | UUID | No | Must reference a zone available in the same workspace. |
| `propagationDate` | date | Yes | Date the batch entered propagation. |
| `quantityInitial` | integer | Yes | HTTP request validation requires a positive value; the service additionally protects non-negative business state. |
| `quantityCurrent` | integer | Yes | Must be zero or greater and cannot exceed the initial quantity. |
| `unitCost` | decimal | Yes | Must be zero or greater. |
| `status` | string | No | Defaults to `propagating` on creation; allowed values are `propagating`, `growing`, `mature`, `sold`, or `dead`. |

The backend assigns `tenantId`; clients must omit it.

## Greenhouse zones

Base path: `/api/greenhouse-zones`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/greenhouse-zones` | Create a growing zone. |
| `GET` | `/api/greenhouse-zones` | List zones in the workspace. |
| `GET` | `/api/greenhouse-zones/{id}` | Retrieve one zone. |
| `DELETE` | `/api/greenhouse-zones/{id}` | Delete a zone. |

A zone request requires a `name` and accepts an optional `microclimate` JSON value. When omitted or null, the API stores an empty JSON object. The current controller exposes create, list, retrieve, and delete operations; a distinct update endpoint has not yet been implemented.

## Treatment compliance

Base path: `/api/treatments`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/treatments` | Log a treatment against a crop batch. |
| `GET` | `/api/treatments` | List treatments in the workspace. |
| `GET` | `/api/treatments/{id}` | Retrieve one treatment. |
| `DELETE` | `/api/treatments/{id}` | Delete a treatment record. |

### Treatment request fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `batchId` | UUID | Yes | Must identify a batch visible in the current workspace. |
| `productName` | string | Yes | Applied product name. |
| `treatmentType` | string | Yes | Must be `organic` or `chemical`. |
| `appliedDate` | date | Yes | Date of application. |
| `applicationRate` | string | No | Free-form recorded application rate. |
| `withholdingDays` | integer | Yes | Must be zero or greater. |

The service verifies that the referenced batch is visible in the same workspace. PostgreSQL generates and stores `safeShipDate` as `appliedDate + withholdingDays`; it is a compliance control that requires human review in operational use.

## Plant-health diagnostics

Base path: `/api/diagnostics`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/diagnostics` | Create a plant-health observation. |
| `GET` | `/api/diagnostics` | List diagnostic observations. |
| `GET` | `/api/diagnostics/{id}` | Retrieve one observation. |
| `POST` | `/api/diagnostics/{id}/review` | Mark an observation reviewed using the verified reviewer identity. |

### Diagnostic request fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `batchId` | UUID | Yes | Must reference a crop batch in the current workspace. |
| `symptomNotes` | string | No | Field observation narrative. |
| `photoUrl` | string | No | Stored media reference; upload/storage lifecycle is a separate concern. |
| `cvPrediction` | string | No | Advisory diagnostic suggestion only. |
| `cvConfidence` | decimal | No | Advisory confidence value only. |

Every created diagnostic sets `needsReview=true`, regardless of any prediction or confidence value.

## Batch events

Base path: `/api/batch-events`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/batch-events` | Record an append-only field event. |
| `GET` | `/api/batch-events` | List workspace event history. |
| `GET` | `/api/batch-events/batch/{batchId}` | List events for one crop batch. |

### Batch event request fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `batchId` | UUID | Yes | Must identify a batch visible to the verified tenant. |
| `eventType` | string | Yes | Event category, such as treatment, mortality, move, or stage change. |
| `payload` | JSON value | No | Event-specific structured data; omitted or null payloads are stored as an empty JSON object. |
| `occurredAt` | UTC instant | No | Defaults to the server time if omitted. |

The backend derives `recordedBy` from the authenticated principal. `batch_events` are protected at the database level from updates and deletion.

## Customer orders

Base path: `/api/orders`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/orders` | Create a customer order. |
| `GET` | `/api/orders` | List workspace orders. |
| `GET` | `/api/orders/{id}` | Retrieve one order. |
| `POST` | `/api/orders/{id}/line-items` | Add a crop commitment to an existing order. |
| `GET` | `/api/orders/{id}/line-items` | List line items for an order. |

### Order request fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `customerName` | string | Yes | Customer or organization name. |
| `orderType` | string | Yes | Commercial workflow category, such as standard, wholesale, or contract grow. |

### Order line-item request fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `batchId` | UUID | No | If set, must be a batch in the same workspace. |
| `species` | string | Yes | Promised crop species. |
| `promisedQty` | integer | Yes | Positive quantity. |
| `promisedDate` | date | No | Expected fulfillment date. |

The service verifies that the parent order and any referenced batch belong to the same workspace before persisting a line item.

## Response shape

Successful create, retrieve, and update operations return the persisted domain record directly as JSON rather than an envelope. List operations return a JSON array. Server-managed fields such as `id`, `tenantId`, `createdAt`, `syncedAt`, `recordedBy`, `reviewedBy`, and generated `safeShipDate` are response data and must not be treated as caller-owned inputs.

| Resource | Important returned fields |
|---|---|
| Plant batch | `id`, `tenantId`, species, zone, propagation date, quantities, unit cost, status, `createdAt`. |
| Greenhouse zone | `id`, `tenantId`, name, microclimate profile. |
| Batch event | `id`, `tenantId`, batch, event type, payload, occurrence/sync time, `recordedBy`. |
| Treatment | `id`, `tenantId`, batch, product, treatment type, applied date, rate, withholding days, generated `safeShipDate`. |
| Diagnostic | `id`, `tenantId`, batch, observation/advisory fields, `needsReview`, reviewer, `createdAt`. |
| Order / line item | IDs, tenant, commercial fields, status/created time or promise/allocation fields. |

## Error responses

The backend's exception handler returns actionable client errors for validation and cross-record workflow failures. Examples include a selected batch or greenhouse zone not being available in the current workspace, invalid crop quantities, or a missing tenant context.

| Status | Meaning | Client response |
|---|---|---|
| `400` | Invalid request or workflow validation failure | Show the returned message near the relevant action or field. |
| `401` | Missing, expired, or invalid authentication | Clear stale session state and route through the configured sign-in flow. |
| `403` | Caller sent an `X-Tenant-ID` that conflicts with the verified token tenant | Remove the client-selected header; tenant scope is claim-derived. |
| `404` | Requested record not found in current workspace | Do not reveal whether the record exists in another tenant. |
| `503` | Frontend gateway cannot reach backend | Preserve safe cached reads when available; show a retryable service-unavailable state. |

## Integration rules

The repository does not currently publish a general third-party integration API. Any future integration must use a service-issued token with verified tenant scope. It must not set tenant context directly, update batch events, or treat diagnostic suggestions as automated operational approval. New API endpoints must follow the same tenant-aware service and database policy pattern before exposure.
