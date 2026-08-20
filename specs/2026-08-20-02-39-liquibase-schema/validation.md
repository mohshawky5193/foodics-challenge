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

- Point the app at a **persistent** H2 file URL (`DB_URL=jdbc:h2:file:./target/liquibase-check`), run
  `mvn spring-boot:run`, stop it, and start it again. **Done** — first run applied all 10 changeSets
  (`New row inserted into product` ×2, `ingredient` ×4, `product_ingredient` ×6, plus the two
  `ALTER SEQUENCE` changes) and started cleanly (`Started FoodicsCodingChallengeApplication`); second
  run logged `Database is up to date, no changesets to execute` (`Run: 0, Previously run: 10`) and
  still started cleanly — Hibernate's `validate` accepted the pre-existing schema unchanged. Confirms
  "a fresh database is built entirely by Liquibase" and "a second start is a no-op" together, which the
  fresh-per-test-run in-memory H2 instance can't demonstrate on its own.
- Seeded row ids were not independently queried via a SQL client — the changelog's `insert` changes
  assign `id: 1`/`2`/etc. literally (visible directly in `002-seed-catalogue-data.yaml`), and Liquibase
  logged a successful `New row inserted` for each, so this was accepted as sufficient without a second
  query round-trip.

## Merge criteria

- [x] `mvn test` passes against the migrated H2 schema
- [x] A fresh database is built entirely by Liquibase (no `DatabaseInitializer`, no `ddl-auto: create`)
- [x] A second start against the same database is a no-op (manual check above)
- [x] `validate` finds no drift (`mvn clean verify` / `spring-boot:run` boots without a
      `SchemaManagementException`)

## Rollback

`git revert` the phase 9 commits — reintroduces `DatabaseInitializer`, `ddl-auto: create-drop`, and
removes the `liquibase-core` dependency and changelogs; since `create-drop` recreates the schema on
every start, there's no migrated-data state to reconcile on the way back.
