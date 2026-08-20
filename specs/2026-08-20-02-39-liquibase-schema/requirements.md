# Phase 9 — Liquibase-managed schema

## Context

`spring.jpa.hibernate.ddl-auto: create-drop` (`src/main/resources/application.yaml`) means Hibernate
infers the schema from the entity annotations and rebuilds it — dropping all data — on every start.
`DatabaseInitializer` (`src/main/java/com/foodics/challenge/config/DatabaseInitializer.java`), a
`CommandLineRunner`, then reseeds the two products, four ingredients, and six recipe rows every time.
There is no reviewable record of the schema and no way to add a column without editing an entity and
hoping Hibernate's inference does the right thing.

This phase adds `liquibase-core`, writes a changelog that reproduces the schema Hibernate currently
generates, and switches `ddl-auto` to `validate` so Hibernate checks the migrated schema instead of
creating it. Seed data moves from `DatabaseInitializer` into a changeset, so the catalogue survives a
restart and `DatabaseInitializer` goes away.

## Scope

- Liquibase added to `pom.xml` via `spring-boot-starter-liquibase` (version managed by the Spring Boot
  4.1.0 parent) — Boot 4 modularized autoconfiguration the same way phase 7 found for JPA/web (see
  Decisions: plain `liquibase-core` resolves but its autoconfiguration never fires).
- `db/changelog/db.changelog-master.yaml` — Spring Boot's default changelog location, so no
  `spring.liquibase.change-log` override is strictly required, but it is set explicitly for
  discoverability.
- A baseline changelog reproducing the current Hibernate-generated schema: the `product`, `ingredient`,
  `order_data`, `product_ingredient`, `order_detail` tables, their primary/foreign keys, and the
  `product_id_seq` / `ingredient_id_seq` / `order_id_seq` sequences (`start 1, increment 50`, matching
  the JPA default `allocationSize` the `@SequenceGenerator`s never override).
- A seed changelog, tagged with a `seed-data` context, inserting the same two products, four
  ingredients, and six `product_ingredient` recipe rows `DatabaseInitializer` inserts today — with the
  same ids the current sequence-driven inserts produce (`Burger`=1, `Chicken Burger`=2; `Beef`=1,
  `Chicken`=2, `Cheese`=3, `Onion`=4), since `OrderRequestUtils` (test fixtures) hardcodes product ids
  `1L`/`2L`.
- `ddl-auto` changed from `create-drop` to `validate`.
- `DatabaseInitializer` deleted; `OrderServiceIntegrationTest`'s `@Import` and its now-unused import
  updated accordingly.
- Tests run the same changelog against H2 (already true structurally — Liquibase autoconfiguration
  runs against whatever `DataSource` Spring wires, including the embedded one `@DataJpaTest`
  substitutes in).

## Out of scope

- Restaurants, suppliers, users, or any new entity/column (Phase 10).
- Any change to `OrderService`/`IngredientService` business logic.
- A generated Liquibase diff/snapshot tool — the changelog is hand-written to match the known,
  already-verified Hibernate output (captured from a recent test run's SQL log).

## Decisions

- **The seed changeset must NOT set `consumed_amount_in_grams` at all — not even to `0`.** This is the
  one place fidelity to current behaviour is easy to get wrong. `DatabaseInitializer` never sets that
  field on a freshly built `Ingredient`, so it inserts as SQL `NULL` (Hibernate inserts every mapped
  column, including nulls). `OrderServiceIntegrationTest.saveOrderEmail` orders only product 1
  (touching Beef/Cheese/Onion) and then asserts exactly **3** ingredients have a non-null
  `consumed_amount_in_grams` — which only holds if the untouched Chicken ingredient is still `NULL`,
  not `0`, after seeding. The baseline changelog therefore drops the entity's
  `columnDefinition = "int default 0"` DB-level default for that column (irrelevant to
  `ddl-auto: validate`, which doesn't check column defaults, and actively wrong here), and the seed
  `insert` change simply omits the column so it comes in as `NULL`.
- **Explicit ids in the seed insert, not Hibernate-generated ones.** Liquibase inserts rows directly, so
  the changeset assigns `id: 1`/`2`/etc. by hand rather than relying on a sequence `nextval()`, matching
  what the current `@SequenceGenerator`-driven inserts happen to produce today.
- **Sequences advanced past the seeded ids after the seed insert**, via a portable `RESTART WITH 51`
  `sql` change on `product_id_seq` and `ingredient_id_seq` (both used an allocation size of 50; 51 is
  the first id outside that block). Nothing in the current codebase creates a `Product` or `Ingredient`
  after startup, so this can't collide today, but leaving the sequences unadvanced would be a landmine
  for the first future feature that does insert one. `order_id_seq` is untouched — no orders are seeded.
- **One changelog file for the baseline schema, one for seed data**, each containing several small
  `changeSet`s (one per table, since foreign keys need their referenced table to exist first) — matches
  the roadmap's "one changelog per change," where "change" means the baseline and the seed data as two
  distinct units, not one changeSet apiece.
- **Column types are the portable Liquibase types (`BIGINT`, `VARCHAR(255)`, `INT`, `TIMESTAMP`)**, not
  H2- or Postgres-specific syntax, so the same changelog produces a schema Hibernate validates
  identically on either database — required for "tests run the same changelog against H2" to also mean
  something for the Postgres path this phase doesn't exercise directly.
- **`DatabaseInitializer` is deleted outright, not deprecated or kept for reference.** Nothing calls it
  once seeding moves to Liquibase, and an unused `CommandLineRunner` sitting in the codebase is a trap
  for the next reader wondering which of the two seeding paths is authoritative.
- **Rolled out in two commits (schema first, seed second) rather than one.** Landing the baseline
  changelog with `ddl-auto: validate` while `DatabaseInitializer` still seeds via JPA proves the
  hand-written schema actually matches what Hibernate expects, in isolation from the seed-migration
  change. Only once that's green does seeding move.

- **`spring-boot-starter-liquibase`, not raw `liquibase-core`.** Adding just `liquibase-core` resolves
  fine on the classpath but silently never runs — Boot 4 split `LiquibaseAutoConfiguration` out into its
  own `spring-boot-liquibase` autoconfigure module, bundled by the `spring-boot-starter-liquibase`
  starter, mirroring the JPA/web starter restructuring phase 7 already had to reconcile. Confirmed by
  running with plain `liquibase-core` first: Hibernate's `validate` failed with "missing table
  [ingredient]" and there was no Liquibase log output at all; switching to the starter produced the
  expected `Running Changeset: ...` log lines and a passing `validate`.
- **`h2` moved off `test` scope, onto the main classpath.** Phase 8 defaulted `DB_URL`/
  `DB_DRIVER_CLASS_NAME` to an in-memory H2 database precisely so a bare `mvn spring-boot:run` needs no
  setup — but `h2` was still `<scope>test</scope>`, so that default could only ever work inside a test
  JVM. Liquibase's default path depends on the H2 fallback actually being bootable, so this phase drops
  the test scope restriction as a prerequisite fix.

## Open questions

None — the roadmap bullets and the existing schema (confirmed from a live Hibernate DDL log) are
specific enough to execute directly.
