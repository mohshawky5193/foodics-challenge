# Phase 7 — Java 21 and Spring Boot 4

## Context

The application is pinned to Java 17 and Spring Boot 3.3.1 (`pom.xml:8,30`), a line that is out of
open-source support. Everything phases 8–14 add — Liquibase, Spring Security's resource server,
`spring-security-test` — is picked from the current generation's dependency management, so upgrading
first means each later phase resolves its versions from the parent instead of pinning them by hand
against an old BOM.

Nothing about the domain changes here. `POST /order` keeps its contract, `IngredientService` keeps
the 50% threshold rule, `EmailService` keeps its `@Async` Gmail send. This phase moves the floor the
code stands on and adapts the code where Boot 4 removed what it was standing on.

Three removals in Boot 4 are already visible in this repository and are not optional:

- `@MockBean` (deprecated in Boot 3.4, removed in 4.0) is used at `OrderControllerTest.java:31` and
  `OrderServiceIntegrationTest.java:33`, imported from `org.springframework.boot.test.mock.mockito`
  — a package that no longer exists.
- Jackson 3 replaces Jackson 2 as the auto-configured mapper. `OrderControllerTest.java:11` imports
  `com.fasterxml.jackson.databind.ObjectMapper` and autowires it at line 35; the bean the context
  publishes is `tools.jackson.databind.ObjectMapper`.
- `hibernate-validator` is pinned explicitly at `pom.xml:54-58`, and the only things importing the
  validation API today are two unused `jakarta.validation.constraints.Min` imports
  (`Ingredient.java:11`, `ProductIngredient.java:9`) — dead imports that will nonetheless fail to
  compile if the API leaves the classpath.

The Java 21 side is already provisioned: `java -version` on this machine reports OpenJDK 21.0.7 and
`JAVA_HOME` points at it, so raising `java.version` needs no toolchain install.

## Scope

- `pom.xml` on `spring-boot-starter-parent` 4.0.x and `<java.version>21</java.version>`.
- Starter coordinates reconciled against the Boot 4.0 migration guide, with the explicit
  `hibernate-validator` and `postgresql` versions dropped so the parent manages them.
- Test sources migrated off removed APIs: `@MockBean` → `@MockitoBean`, Jackson 2 `ObjectMapper` →
  Jackson 3.
- `OrderRequest` and `ProductRequest` converted to records, with `OrderService`, `OrderControllerTest`,
  and `OrderRequestUtils` following.
- `README.md` and `specs/tech-stack.md` updated so the stated runtime matches the built one, with the
  run instructions calling for Docker instead of an installed PostgreSQL.
- `mvn clean verify` green on Java 21 with the same three integration tests and two controller tests
  asserting the same behaviour.

## Out of scope

- **Virtual threads.** Java 21 makes `spring.threads.virtual.enabled` available; enabling it changes
  the `@Async` execution model and belongs with the email work in Phase 14, not with a version bump.
- **`ddl-auto` and the schema.** Still `create-drop`. Liquibase is Phase 8.
- **New validation.** `spring-boot-starter-validation` goes on the classpath, but no `@Valid`, no
  `@Positive` on `ProductRequest`. Request validation is a Phase 11 line item, and adding it here
  would change the endpoint's contract inside a "no behavioural change" phase.
- **The `MERCHANT_EMAIL` constant** (`IngredientService.java:21`). Phase 12 replaces it.
- **Pre-existing defects.** `DatabaseInitializer.createProductIngredientKey` sets `ingredientId`
  twice and never sets `productId` (`DatabaseInitializer.java:87-91`), and
  `OrderService.createOrderWithOrderDetails` attaches an empty `OrderDetailsKey`
  (`OrderService.java:65`). Both are wrong today and will be equally wrong after the upgrade. They
  are recorded in Open questions, not fixed here — a migration whose diff also contains behaviour
  fixes cannot be reviewed as a migration.

  **Amended during implementation.** A *third* pre-existing defect, not anticipated above, was
  surfaced by validation step 3 and — by explicit decision — **is** fixed on this branch, in its own
  commit. See the "Stock rejection fixed despite the out-of-scope rule" decision below. The two
  composite-key defects remain out of scope and untouched.

## Decisions

**Boot 4.0.x, not 3.5.x.** The intermediate step would be the cautious choice on a large codebase;
this one is nine main classes and four tests. Going straight to 4.0 pays the module-restructuring and
Jackson-3 cost once. Rejected: a 3.3 → 3.5 → 4.0 ladder, which triples the verification work for a
codebase this size.

**The migration guide is authority, not memory.** Boot 4 restructured autoconfiguration into
per-technology modules and renamed several starters. The exact 4.0.x patch version and the exact
starter coordinates are resolved from Maven Central and the official migration guide at implementation
time and written into `pom.xml` — not assumed from this document. The build failing to resolve a
coordinate is the cheap failure; a coordinate that resolves to something subtly different is not.

**`@MockitoBean`, not a hand-rolled test configuration.** `org.springframework.test.context.bean.override.mockito.MockitoBean`
is the direct replacement and keeps both tests' structure identical. Rejected: replacing
`@DataJpaTest` + `@Import` in `OrderServiceIntegrationTest` with a constructor-injected stub, which
would be a test rewrite riding along in a version bump.

**Records for the two request models only.** `OrderRequest` and `ProductRequest` are immutable
data carried one way from the wire into `OrderService` — exactly what records are for, and Jackson 3
binds them without extra annotations. The knock-on is mechanical: `ProductRequest::getProductId`
becomes `ProductRequest::productId` (`OrderService.java:37-38`) and `OrderRequestUtils` constructs
instead of setting. Entities stay classes — JPA requires a no-arg constructor and mutable fields.

**No pattern matching in `FoodicsChallengeControllerAdvice`.** The roadmap offers it as a candidate;
the advice has exactly one `@ExceptionHandler` for one exception type
(`FoodicsChallengeControllerAdvice.java:13-16`). Introducing `instanceof` patterns there would mean
first collapsing typed handlers into an untyped one, which is worse code justified by a language
feature. The roadmap's own qualifier — "only where they clarify existing code" — settles it.

**Green baseline before the version changes.** The first commit records what `mvn clean verify`
does on the current pom, so any failure after the bump is attributable. This matters because
`FoodicsCodingChallengeApplicationTests` is a plain `@SpringBootTest` with no `@ActiveProfiles`
(`FoodicsCodingChallengeApplicationTests.java:6`), which means it resolves `application.yaml` and
tries to reach PostgreSQL 
at `localhost:5432` — it may well be red today, before Boot 4 touches
anything.

**PostgreSQL from a throwaway container, with no volume.** Manual verification runs against
`docker run --rm ... postgres:17-alpine` rather than a local install. The reason is not convenience:
`DatabaseInitializer` is a `CommandLineRunner` that seeds unconditionally, so on a database that
survives between runs it is impossible to tell a successful seed from data an earlier run left
behind. Omitting the volume makes every start a genuinely empty database, which is also what
`ddl-auto: create-drop` already assumes. Rejected: mounting a named volume "in case the data is
wanted" — persisted data is precisely what would make this check meaningless. Testcontainers was also
rejected, for now: it would bring PostgreSQL into `mvn test` and change what the suite requires, and
this phase is not the place to alter the test setup.

**Stock rejection fixed despite the out-of-scope rule.** *(Added during implementation, reversing the
"pre-existing defects" exclusion for this one case.)* Validation step 3 failed: an order for 100
burgers returned `200` and drove Onion to 2080g consumed against 1000g of stock. The cause is the
`else if` at `IngredientService.java:39-43` — an ingredient that crosses the 50% threshold on the
same order takes the alert branch, so the over-100% check is never evaluated. `IngredientService` is
byte-for-byte unchanged on this branch, so the migration did not cause it; Phase 7's "no behavioural
change" premise holds either way. It is fixed here anyway, in its own labelled commit, because it
silently corrupts stock and contradicts a mission success criterion. The two checks are now
independent, insufficiency evaluated first. Rejected: deferring to a later phase, which would have
left a known data-integrity hole open for the sake of a clean diff.

**A regression test for the rejection path, not just the fix.** The bug survived because nothing
exercised a real rejection: `OrderControllerTest.postOrderWithException` mocks `OrderService` and
injects the exception with `doThrow`, and `OrderServiceIntegrationTest` had no rejection case at all.
`rejectOrderExceedingStock` closes that gap and was confirmed to fail against the pre-fix service
("Expected InsufficientIngredientsException to be thrown, but nothing was thrown") and pass with it.
It also asserts no alert email is sent, since the buggy path sent one.

**Version pins dropped, both of them.** `postgresql` 42.7.3 (`pom.xml:42-46`) and
`hibernate-validator` 8.0.1.Final (`pom.xml:54-58`) both become parent-managed —
`hibernate-validator` by way of `spring-boot-starter-validation` rather than as a direct dependency,
so the starter owns the transitive `jakarta.validation-api` the two `@Min` imports need.

## Open questions

1. **Is `mvn clean verify` green today?** `FoodicsCodingChallengeApplicationTests` looks like it
   requires a running PostgreSQL and mail configuration. If it is already failing, this phase should
   either add `@ActiveProfiles("test")` to it — a one-line fix that makes the baseline meaningful —
   or the merge criteria must be stated against `mvn test -Dtest='Order*'` instead. Answer before
   task group 1 is committed.
2. **The two composite-key defects.** They are out of scope above, but if the upgrade to Hibernate 7
   turns either into a hard failure (Hibernate 7 is stricter about `@MapsId` and partially populated
   `@EmbeddedId` values than Hibernate 6 was), the fix stops being optional. Decide then: fix in a
   separate commit on this branch, clearly labelled, or split into its own phase.
3. **Does the Maven wrapper need raising?** It is pinned to Maven 3.9.7
   (`.mvn/wrapper/maven-wrapper.properties`). Confirm 4.0.x builds under it; raise the wrapper only
   if it does not.
