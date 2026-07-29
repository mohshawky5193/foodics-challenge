      # Phase 7 — Java 21 and Spring Boot 4 — Plan

## 1. Baseline and Java 21

Establishes what green means before the framework moves, and takes the runtime step on its own so a
Java-21 failure and a Boot-4 failure cannot be confused.

- [x] Run `mvn clean verify` on the unchanged pom and record the result — pass/fail per test class —
      in the branch's first commit message
- [x] If `FoodicsCodingChallengeApplicationTests` fails for want of PostgreSQL, resolve Open question 1
      (add `@ActiveProfiles("test")` to it, or restate the baseline) before continuing
      — **answered:** neither. `contextLoads` runs against the throwaway container, with credentials
      supplied as `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` environment variables so
      nothing is committed. Baseline without the container: 5/6, `contextLoads` erroring on
      "Unable to determine Dialect without JDBC metadata". With it: 6/6.
- [x] Raise `<java.version>` from 17 to 21 in `pom.xml:30`
- [x] Re-run `mvn clean verify` on Boot 3.3.1 + Java 21 and confirm the baseline is unchanged
      — 6/6, unchanged

## 2. Boot 4 dependency reconciliation

*Depends on 1.*

The pom-only step. No `src/` edits here; the build is expected to fail compilation at the end of this
group, and group 3 is what makes it pass.


- [x] Resolve the current `spring-boot-starter-parent` 4.0.x release from Maven Central and set it at
      `pom.xml:8` — **superseded:** Maven Central showed 4.0.7 as the newest 4.0.x but 4.1.0 as the
      current release. Asked, and **4.1.0** was chosen over following `4.0.x` literally.
- [x] Reconcile each starter against the Spring Boot 4.0 migration guide —
      `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `spring-boot-starter-mail`,
      `spring-boot-starter-test` — and correct any coordinate Boot 4 renamed or split
      — one rename: `spring-boot-starter-web` → `spring-boot-starter-webmvc`. `-data-jpa`, `-mail`,
      and `-test` resolve unchanged.
- [x] Replace the direct `hibernate-validator` dependency (`pom.xml:54-58`) with
      `spring-boot-starter-validation`, no version — pulls `hibernate-validator:9.1.0.Final`
- [x] Drop the explicit `<version>42.7.3</version>` from the `postgresql` dependency (`pom.xml:45`)
      — parent now resolves it to 42.7.11
- [x] Confirm `mvn -version` runs 4.0.x under the wrapper's Maven 3.9.7; raise
      `.mvn/wrapper/maven-wrapper.properties` only if it does not (Open question 3)
      — **answered:** `./mvnw clean compile` succeeds under 3.9.7. No raise needed.
- [x] Run `mvn dependency:tree` and confirm Jackson 3 (`tools.jackson`), Hibernate 7, and Spring
      Framework 7 are what resolved — `tools.jackson.core:jackson-databind:3.1.4`,
      `hibernate-core:7.4.1.Final`, `spring-core:7.0.8`. Only `jackson-annotations:2.21` keeps the
      `com.fasterxml.jackson.core` group, which the migration guide names as the documented exception.

## 3. Main source adaptation

*Depends on 2.*

- [ ] Fix any main-source compilation error the Boot 4 upgrade surfaces — removed or relocated Spring
      APIs across `OrderController`, `FoodicsChallengeControllerAdvice`, the four services, and
      `DatabaseInitializer`
- [ ] Remove the unused `org.springframework.web.context.annotation.RequestScope` import at
      `OrderService.java:18`
- [ ] Remove the unused `jakarta.validation.constraints.Min` imports at `Ingredient.java:11` and
      `ProductIngredient.java:9`
- [ ] Start a throwaway PostgreSQL — `docker run --rm -d --name foodics-pg -e POSTGRES_USER=foodics
      -e POSTGRES_PASSWORD=foodics -e POSTGRES_DB=foodics -p 5432:5432 postgres:17-alpine`, no volume —
      and point `application-dev.yaml` at it
- [ ] Confirm `mvn spring-boot:run -Dspring-boot.run.profiles=dev` boots against that container and
      `DatabaseInitializer` still seeds two products and four ingredients (see `validation.md` steps 0–1)

## 4. Test source adaptation

*Depends on 3.*

Where Boot 4's removals actually bite. Each change is a substitution — no test's assertions change.

- [ ] Replace `@MockBean` with `@MockitoBean` at `OrderControllerTest.java:31` and
      `OrderServiceIntegrationTest.java:33`, importing
      `org.springframework.test.context.bean.override.mockito.MockitoBean` in place of
      `org.springframework.boot.test.mock.mockito.MockBean`
- [ ] Switch `OrderControllerTest.java:11` from `com.fasterxml.jackson.databind.ObjectMapper` to
      `tools.jackson.databind.ObjectMapper`, keeping the autowired mapper at line 35
- [ ] Remove the unused `org.mockito.Mockito.verify` and `ArgumentMatchers.eq` static imports at
      `OrderControllerTest.java:5-6`
- [ ] Fix any remaining test compilation error from Boot 4's test-support changes, leaving
      `@WebMvcTest`, `@DataJpaTest`, `@Import`, and `@ActiveProfiles` semantics as they are
- [ ] Confirm all five existing tests pass with unchanged assertions

## 5. Records for the request models

*Depends on 4.*

Kept last and separate, so a reviewer can read the migration without the refactor mixed in, and so a
revert of the refactor does not revert the upgrade.

- [ ] Convert `OrderRequest` to `record OrderRequest(List<ProductRequest> products)`
- [ ] Convert `ProductRequest` to `record ProductRequest(Long productId, Integer quantity)`
- [ ] Update `OrderService.java:37-38` to the record accessors — `ProductRequest::productId` and
      `productRequest.quantity()`
- [ ] Rewrite the three fixtures in `OrderRequestUtils` to construct records instead of calling
      setters
- [ ] Confirm Jackson 3 still binds the `POST /order` body — `OrderControllerTest.postOrder` is the
      test that proves it

## 6. Documentation

*Depends on 5.*

- [ ] Update `README.md:5` and `README.md:25` from Java 17 to Java 21, and name Spring Boot 4 in the
      stack list
- [ ] Replace "Make sure you have both `Java 17` and `Postgres` installed" (`README.md:25`) with the
      `docker run` one-liner, so a reader needs Java 21 and Docker rather than a local PostgreSQL
- [ ] Move Java 21 and Spring Boot 4.0.x from "Target stack" to "Current stack" in
      `specs/tech-stack.md`, and drop the now-satisfied Java-17 and Boot-4 lines from its Constraints
      and Planned-constraints sections
- [ ] Tick Phase 7's items in `specs/roadmap.md` and record the exact 4.0.x version that landed
