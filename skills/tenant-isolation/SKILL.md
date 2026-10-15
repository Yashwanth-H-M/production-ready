---
name: tenant-isolation
description: Use this skill whenever writing or reviewing backend code that reads or writes tenant-scoped data (anything with a tenant_id column) in the Nursery CRM. Covers the Postgres RLS + Spring Boot pattern that keeps tenant isolation correct under connection pooling — the single highest-risk area in this codebase.
---

# Tenant isolation (RLS + connection pooling)

## The problem this solves

Postgres Row-Level Security only isolates tenants if `app.tenant_id` is
set correctly, in the same transaction, before any query runs. Two common
implementations are both broken in ways that pass code review and fail
under load:

- Setting `app.tenant_id` with a plain `SET` (session-scoped) breaks the
  moment a connection pooler (PgBouncer, etc.) reuses a connection across
  two different tenants' requests mid-session.
- Relying on Spring's `@Transactional` AOP advice ordering to guarantee
  "transaction begins, then tenant context is set" is fragile: whether a
  custom aspect runs before or after the transactional advice opens the
  transaction depends on relative `@Order` precedence, which is easy to
  get backwards and easy to silently regress when someone adds a new
  aspect later.

## The pattern to use instead

Set the tenant with `set_config('app.tenant_id', ?, true)` — the `true`
third argument makes it transaction-local (equivalent to `SET LOCAL`, but
unlike raw `SET LOCAL`, `set_config` takes a normal bind parameter, so
there's no manual string escaping or injection risk). Do this explicitly,
in code you control, inside a `TransactionTemplate.execute(...)` block —
not via `@Transactional` plus AOP timing.

Reference implementation:

```java
@Component
public class TenantAwareExecutor {
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public TenantAwareExecutor(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public <T> T execute(Supplier<T> work) {
        return transactionTemplate.execute(status -> {
            String tenantId = TenantContext.get();
            if (tenantId == null) {
                throw new IllegalStateException(
                    "No tenant in context — refusing to run a tenant-scoped query");
            }
            entityManager.createNativeQuery(
                    "SELECT set_config('app.tenant_id', :tenantId, true)")
                .setParameter("tenantId", tenantId)
                .getSingleResult();
            return work.get();
        });
    }
}
```

Every service method that touches a tenant-scoped table calls
`tenantAwareExecutor.execute(() -> repository.someMethod(...))` instead of
calling the repository directly.

## Checklist for any new tenant-scoped feature

- [ ] Table has `ENABLE ROW LEVEL SECURITY` and a `tenant_isolation`
      policy in `database/schema.sql` (see existing tables for the pattern).
- [ ] Service method routes through `TenantAwareExecutor`, not a bare
      `@Transactional` repository call.
- [ ] `TenantContext.get()` is populated before the executor runs — trace
      it back to `TenantFilter` and confirm the request actually carried
      a tenant claim.
- [ ] A test exists proving tenant A cannot read tenant B's rows for this
      specific table — not just "the happy path returns data."
- [ ] If a connection pooler is introduced later, confirm it isn't
      configured to multiplex a connection across two transactions —
      `set_config(..., true)` is safe under transaction-mode pooling only
      as long as the pooler doesn't hand a connection to a new
      transaction before the previous one commits or rolls back.

## What not to do

- Don't use raw `SET LOCAL app.tenant_id = '...'` with string
  concatenation — injection risk and no bind-parameter support.
- Don't add a second, competing enforcement mechanism (a Hibernate
  filter, a WHERE-clause-injecting aspect) alongside RLS — keep the
  enforcement point at the database layer, since that's what still
  protects you if application code has a bug.
- Don't assume `@Transactional` ordering is correct without a test that
  proves it — this is exactly the kind of thing that works in every
  manual test and fails the first time two tenants hit the API
  concurrently under a pooler.
