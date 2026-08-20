# Phase 9 — Liquibase-managed schema — Validation

## Automated

- `mvn clean verify` — must pass twice in a row without wiping state in between (see Manual, since
  each test run gets a fresh in-memory H2 instance by design). Covers:
  - `FoodicsCodingChallengeApplicationTests` (`@SpringBootTest`) — full context load proves Hibernate's
    `validate` accepts the Liquibase-built schema.
  - `OrderServiceIntegrationTest.saveOrderNoEmail` / `saveOrderEmail` / `rejectOrderExceedingStock` /
    `saveOrderEmailOnceWithMultipleOrderRequests` — exercise the seeded catalogue exactly as before;
    `saveOrderEmail`'s "exactly 3 non-null `consumedAmountInGrams`" assertion is the sharpest check that
    the seed changeset leaves `consumed_amount_in_grams` `NULL`, not `0`.
  - `OrderControllerTest` — unaffected by schema, kept as a regression check.
- No new tests are added this phase — existing tests are the proof that switching the seeding and
  schema mechanism didn't change observable behaviour.

## Manual

- Point the app at a **persistent** H2 file URL (e.g. `DB_URL=jdbc:h2:file:./target/liquibase-check`)
  or a local Postgres, run `mvn spring-boot:run`, stop it, and start it again. Second start must log
  Liquibase skipping every changeSet (`already executed`) and Hibernate's `validate` must still pass —
  demonstrating "a fresh database is built entirely by Liquibase" and "a second start is a no-op"
  together, which a fresh-per-run in-memory H2 test can't demonstrate on its own.
- Query `product`/`ingredient`/`product_ingredient` after the first start and confirm the seeded rows
  and ids match `DatabaseInitializer`'s old output (Burger=1, Chicken Burger=2, Beef=1, Chicken=2,
  Cheese=3, Onion=4).

## Merge criteria

- [ ] `mvn test` passes against the migrated H2 schema
- [ ] A fresh database is built entirely by Liquibase (no `DatabaseInitializer`, no `ddl-auto: create`)
- [ ] A second start against the same database is a no-op (manual check above)
- [ ] `validate` finds no drift (`mvn clean verify` / `spring-boot:run` boots without a
      `SchemaManagementException`)

## Rollback

`git revert` the phase 9 commits — reintroduces `DatabaseInitializer`, `ddl-auto: create-drop`, and
removes the `liquibase-core` dependency and changelogs; since `create-drop` recreates the schema on
every start, there's no migrated-data state to reconcile on the way back.
