# Phase 8 — Environment-variable configuration

## Context

Configuration today is spread across three YAML files under `src/main/resources`:
`application.yaml` (Postgres datasource, mail host/port, JPA settings), `application-dev.yaml.example`
templating a gitignored `application-dev.yaml` (datasource and Gmail credentials, selected via
`-Dspring-boot.run.profiles=dev`), and `application-test.yaml` (H2 datasource, selected via
`@ActiveProfiles("test")` in `OrderServiceIntegrationTest`). Credentials live in an uncommitted
per-developer file, and the test profile owns a whole parallel config file just to point at H2.

This phase collapses configuration to one file — `application.yaml` — with every environment-specific
value read from an environment variable, so nothing environment-specific is committed and there is no
per-profile YAML to keep in sync.

## Scope

- `application.yaml` is the only Spring config file; datasource URL, datasource driver, datasource
  credentials, and mail host/port/credentials are all `${VAR_NAME}` placeholders.
- `mvn test` runs against H2 using environment variables supplied by Surefire (`pom.xml`
  `<environmentVariables>`), not a YAML profile.
- `application-dev.yaml.example`, the local `application-dev.yaml`, and `application-test.yaml` are
  deleted; the `.gitignore` entry for `application-dev.yaml` is removed.
- `@ActiveProfiles("test")` is dropped from `OrderServiceIntegrationTest` since the `test` profile no
  longer selects anything.
- README's configuration section documents the required environment variables and how to set them.

## Out of scope

- Anything about the schema itself (Liquibase is Phase 9) — `ddl-auto: create-drop` stays as-is.
- Changing the mail transport (Gmail SMTP stays; Resend/Brevo is Phase 15).
- Restaurants, suppliers, users (Phase 10).

## Decisions

- **Datasource URL and driver class default to H2, in-memory.** The roadmap calls out "the H2 URL for
  tests" as the example of a local-friendly inline default. Making H2 the *default* for both `DB_URL`
  and `DB_DRIVER_CLASS_NAME` means a bare `mvn test` (or even `mvn spring-boot:run` with no env vars
  beyond credentials) runs against H2 with zero setup; pointing at Postgres for a real deployment is one
  env var each. This also removes the need for a `spring.jpa.database-platform` override — Spring Boot's
  Hibernate autoconfiguration detects the dialect from the driver/URL at connection time for both H2 and
  Postgres, so no `DB_DIALECT` variable is introduced.
- **Credentials get no inline default, anywhere.** `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`,
  `MAIL_PASSWORD` are `${VAR}` with no fallback, so a missing credential fails startup loudly instead of
  silently running with a baked-in value. This applies uniformly — even the H2 `sa` user is supplied
  through `DB_USERNAME`, not hardcoded — so there's one rule ("credentials always come from the
  environment") instead of a local exception for H2.
- **Mail host/port keep the current Gmail values as inline defaults.** They aren't secrets, and changing
  the transport is explicitly Phase 15's job, not this phase's.
- **Test env vars supplied via Surefire, not relied on for defaults alone.** Even though `DB_URL`/
  `DB_DRIVER_CLASS_NAME` already default to H2, `pom.xml` sets them explicitly for `mvn test` (per the
  roadmap bullet), plus the credential vars that have no default (`DB_USERNAME=sa`, `DB_PASSWORD`
  (empty), `MAIL_USERNAME`/`MAIL_PASSWORD` dummy values) — `FoodicsCodingChallengeApplicationTests` loads
  a full `@SpringBootTest` context, which needs mail properties to be present even though it never sends
  anything.
- **`spring.profiles.active` / `@ActiveProfiles` dropped entirely.** Nothing remains profile-specific
  once dev and test stop owning YAML, so `@ActiveProfiles("test")` comes out of
  `OrderServiceIntegrationTest` and the README's `-Dspring-boot.run.profiles=dev` instruction goes away.
- **Batch-size/JPA property differences between the old prod and test YAML are not preserved.** The old
  test file used `batch_size: 10` vs. 100 in prod; that split wasn't environment-driven configuration in
  the sense this phase cares about, so the merged file just keeps one value (100).
- **The stray local `application-dev.yaml` file is deleted from disk, not just untracked.** It's a
  generated-from-template credentials file with no history of its own (it was already gitignored); once
  `application-dev.yaml.example` is gone, keeping the file it templated around is dead weight.

## Open questions

None — the roadmap bullets are specific enough to execute directly.
