# Canopy Design System

## Design intent

Canopy Operations Cloud should feel like a calm, high-trust operations platform rather than a generic dashboard. Commercial nursery teams need to scan crop state, health exceptions, compliance constraints, and order commitments quickly. The interface therefore uses a dark operational frame, a low-noise content canvas, precise data typography, and a small number of decisive color signals.

> The interface should make the next responsible operational action obvious without suggesting that an unreviewed recommendation is an automatic decision.

## Brand foundations

| Element | Standard | Rationale |
|---|---|---|
| Product name | **Canopy Operations Cloud** | Use consistently in UI, documentation, metadata, and release communication. |
| Brand font | Manrope, weights 500/600/700/800 | Confident hierarchy for navigation, headings, and actions. |
| Body font | Inter | Legible prose, labels, forms, and dense content. |
| Data font | IBM Plex Mono | Distinguishes quantities, dates, identifiers, and metrics. |
| Core color | Canopy `#0F7C61` | Trusted primary action and positive operational state. |
| Accent color | Lime `#D9FF5A` | Sparing high-attention highlight. |
| Structural dark | Ink `#18252A` | Operations rail and high-contrast framing. |
| Canvas | App background `#F5F7F8` | Quiet backdrop for structured panels and data. |

The legacy product names **Nursery CRM** and **Verdant**, the legacy Space Grotesk typography, and retired palette tokens must not be reintroduced in new work.

## Layout architecture

`AppShell` provides a persistent dark operations rail, command header, connectivity state, mobile navigation, and an application content canvas. Workspace routes should use this frame rather than independently inventing global navigation or page width behavior.

| Region | Purpose | Rules |
|---|---|---|
| Operations rail | Primary navigation and product identity | Dark ink background, concise labels, active route clarity, collapsible/responsive behavior. |
| Command header | Page context and highest-priority control | Keep actions limited; surface connection state without visual alarmism. |
| Content canvas | Workspace-specific operational information | Use generous but efficient page padding and consistent panel rhythm. |
| Decision queue | Exceptions needing human action | State the decision required, affected record, due context, and next safe action. |
| Mobile navigation | Field-device access | Preserve primary workflows and avoid dense hover-only controls. |

## Reusable primitives

| Class or pattern | Use | Do not use for |
|---|---|---|
| `e-panel` | Grouped data, forms, decision blocks, and panels | A decorative wrapper without a clear information boundary. |
| `e-primary` | One primary action per local action group | Multiple competing submit buttons. |
| `e-secondary` | Secondary, navigational, or reversible actions | Destructive actions without a distinct safety treatment. |
| `e-input` | Text, numeric, date, and multiline form inputs | Read-only metric display. |
| `e-select` | Controlled choice with visible label | Free-form data where typed search is needed. |
| `e-table-head` | Table column headers | General subheadings. |
| `e-table-cell` | Dense operational tabular content | Primary page headings. |
| `e-eyebrow` | Small uppercase contextual label above a title | Essential information that cannot be omitted. |
| `e-title` | Page and section titles | Tiny status labels or field captions. |

A component should become a shared primitive when it recurs across routes with the same semantic behavior. Otherwise, keep it feature-local to preserve clarity and avoid an oversized abstract component library.

## Typography and density

Use Manrope for moments that establish hierarchy, Inter for reading and interaction, and IBM Plex Mono for operational values. Tables should be dense enough for inventory management but retain row focus, alignment, sufficient line height, and horizontal treatment at small widths.

| Content type | Typography guidance |
|---|---|
| Workspace title | Manrope, strong weight, short verb-or-noun phrasing. |
| Panel title | Manrope or Inter semibold, meaningful label. |
| Metric value | IBM Plex Mono or Manrope, consistent numeric alignment. |
| Table data | Inter; use IBM Plex Mono for codes, dates, and compact numeric values. |
| Field label/help | Inter; labels remain visible and never rely on placeholder text alone. |
| System status | Short, direct language with visible semantic state. |

## Color and status semantics

Color should reinforce meaning, never carry it alone. Pair a color token with text, icon, shape, or placement. Lime is intentionally rare and should draw attention to a priority, signal, or small badge rather than cover large areas.

| Meaning | Treatment |
|---|---|
| Primary action / trusted completion | Canopy green plus explicit action label. |
| High-attention planning signal | Lime accent, supporting text, and limited visual footprint. |
| Review required | Clear review label and queue placement; do not imply automated approval. |
| Compliance or ship-hold concern | Plain-language restriction and a human-safe next step. |
| Error | High-contrast error state with actionable recovery wording. |
| Offline/cached | State that data may be stale; do not visually simulate a confirmed live state. |

## Forms and validation

Operational forms should be predictable, sparse, and explicit. Group fields by the decision they support, preserve labels above controls, validate constraints near the field, and maintain backend authority. A loading state must prevent accidental duplicate submissions while retaining enough context for the operator to understand what is pending.

Never expose fields that allow a user to choose security facts such as tenant ownership, event recorder, reviewer identity, or trusted ship approval. The backend derives these facts from context and workflow.

## Data tables and decision queues

Tables are for scanning and comparison. The first columns should identify the operational object, while status, risk, date, quantity, and action columns should follow in stable order. Avoid placing important information behind hover-only affordances. On small screens, prioritize the primary identity, the most consequential state, and a clear drill-in action.

Decision queues should provide a human-readable reason, business impact, accountable next step, and direct route into the relevant workflow. They should never use fear-inducing language or automatically resolve diagnostic and compliance signals.

## Motion

Motion is subtle and functional. Use short transitions to explain panel arrival, state changes, navigation, and controls. Do not make loading, form submission, modal dismissal, or error recovery depend on long animations. Respect reduced-motion preferences and never use animation as the sole cue for a state change.

## Accessibility and quality bar

Every view requires semantic landmarks, logical headings, visible keyboard focus, labelled controls, button names, contrast-aware color use, and touch-friendly targets. Test at desktop and narrow mobile widths. Ensure empty, loading, error, offline, and success states are designed rather than relying on raw browser errors or blank space.

## Content voice

Canopy's voice is concise, factual, and responsible. Prefer `Review 3 plant-health observations` over vague labels such as `Attention required`. Use the crop, zone, date, and action terms an operator needs. Do not overstate advisory AI output, shipment readiness, or stored data freshness.

Read [FRONTEND.md](FRONTEND.md) for implementation structure and [API.md](API.md) for user-visible error semantics.
