# Phase 8 — Environment-variable configuration — Validation

## Automated

- `mvn clean verify` — must pass with **no environment variables set in the shell**, relying solely on
  the H2 inline defaults plus the Surefire-injected credential vars. Covers `FoodicsCodingChallengeApplicationTests`
  (`@SpringBootTest`, needs a working datasource and valid mail properties to load context),
  `OrderControllerTest`, and `OrderServiceIntegrationTest` (now without `@ActiveProfiles("test")`).
- No new tests are added this phase — existing tests are the regression check that removing the profile
  YAML didn't change runtime behaviour.

## Manual

- Confirm a fresh clone with no env vars set fails fast and clearly on `mvn spring-boot:run` (missing
  `DB_USERNAME`/`DB_PASSWORD`/`MAIL_USERNAME`/`MAIL_PASSWORD`), rather than silently booting with a
  leftover default.
- Set `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD` (H2 defaults cover the rest) and
  confirm `mvn spring-boot:run` boots against in-memory H2.
- Additionally set `DB_URL=jdbc:postgresql://localhost:5432/foodics`,
  `DB_DRIVER_CLASS_NAME=org.postgresql.Driver` against a local Postgres and confirm it boots there too —
  proves the same `application.yaml` serves both databases through env vars alone.

## Merge criteria

- [ ] `mvn clean verify` passes on Java 21
- [ ] No `application-dev.yaml.example` or profile-specific YAML remains under `src/main/resources` or
      `src/test/resources`
- [ ] Application starts using only environment variables for datasource and mail configuration
- [ ] README alone is enough to configure and run the app in a fresh environment
- [ ] `.gitignore` no longer references `application-dev.yaml`

## Rollback

`git revert` the phase 8 commit(s) — reintroduces the three YAML files and the profile flag; no schema
or data changes are involved, so this is a clean, low-risk revert.
