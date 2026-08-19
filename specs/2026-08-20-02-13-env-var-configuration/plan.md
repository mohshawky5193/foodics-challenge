# Phase 8 — Environment-variable configuration — Plan

## 1. Consolidate `application.yaml`
- [x] In `src/main/resources/application.yaml`, replace the hardcoded `spring.datasource` block with
      `driver-class-name: ${DB_DRIVER_CLASS_NAME:org.h2.Driver}`, `url: ${DB_URL:jdbc:h2:mem:testdb}`,
      `username: ${DB_USERNAME}`, `password: ${DB_PASSWORD}`
- [x] Replace the `spring.mail` block with `host: ${MAIL_HOST:smtp.gmail.com}`,
      `port: ${MAIL_PORT:587}`, `username: ${MAIL_USERNAME}`, `password: ${MAIL_PASSWORD}`, keeping the
      existing `mail.properties.mail.smtp.auth`/`starttls.enable` block as-is
- [x] Keep `spring.jpa` (`generate-ddl`, `show-sql`, `hibernate.ddl-auto: create-drop`, batch/order
      settings) as one shared block — no `database-platform` override, no per-profile split

## 2. Remove the per-profile files
- [x] Delete `src/main/resources/application-dev.yaml.example`
- [x] Delete `src/main/resources/application-dev.yaml` (gitignored, local-only)
- [x] Delete `src/main/resources/application-test.yaml`
- [x] Remove the `/src/main/resources/application-dev.yaml` line from `.gitignore`

## 3. Wire test env vars and drop the test profile
*Depends on 1, 2.*
- [x] Add a `maven-surefire-plugin` `<configuration><environmentVariables>` block to `pom.xml` setting
      `DB_DRIVER_CLASS_NAME=org.h2.Driver`, `DB_URL=jdbc:h2:mem:testdb`, `DB_USERNAME=sa`,
      `DB_PASSWORD=` (empty), `MAIL_USERNAME`/`MAIL_PASSWORD` to placeholder test values
- [x] Remove `@ActiveProfiles("test")` and its import from `OrderServiceIntegrationTest`

## 4. Update docs
*Depends on 1, 2, 3.*
- [x] Rewrite README's "How to run" section: list the required environment variables
      (`DB_DRIVER_CLASS_NAME`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MAIL_HOST`, `MAIL_PORT`,
      `MAIL_USERNAME`, `MAIL_PASSWORD`), note which have local-friendly defaults (H2, Gmail host/port)
      and which don't (all four credentials), and show how to set them (shell `export`, a `.env` file
      loaded by the shell, or an IDE run configuration) instead of copying an example YAML
  - [x] Drop the `-Dspring-boot.run.profiles=dev` instruction (no other profile references existed to
      re-check); also corrected the stale "Java 17" tech-stack line to "Java 21" while touching the file

## 5. Verify
*Depends on 1-4.*
- [x] `mvn clean verify` passes with no env vars set beyond what Surefire injects
- [x] Confirm no file matching `application-dev.yaml*` or `application-test.yaml` remains under
      `src/main/resources` or `src/test/resources`
