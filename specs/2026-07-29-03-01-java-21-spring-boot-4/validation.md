# Phase 7 — Java 21 and Spring Boot 4 — Validation

The whole phase is a negative claim: nothing behaves differently. So validation is mostly the
existing suite, re-run — plus proof that it is really running on the new floor and not on a stale
target directory.

## Automated

Commands that must pass, verbatim:

- `mvn clean verify` — the phase's headline check. `clean` is not optional: a `target/` full of
  Java-17 classes will happily pass a `verify` that never recompiled them.
- `mvn -version` — must report the JVM as 21.x. Run before and after, since a wrapper or `JAVA_HOME`
  change is the likeliest way to think you upgraded and not have.
- `mvn dependency:tree` — inspected, not just run. `tools.jackson:jackson-databind` present and
  `com.fasterxml.jackson.core:jackson-databind` absent from the compile scope; `hibernate-core` 7.x;
  `spring-core` 7.x; `hibernate-validator` present with no version declared in `pom.xml`.

**No new tests.** This phase adds none deliberately — a new test would be new behaviour to explain,
and the point is that there is none. The five existing tests carry the phase, and each must pass with
its assertions untouched:

| Test | What it still asserts |
| --- | --- |
| `OrderControllerTest.postOrder` | `POST /order` with a valid body returns `200`. Under Boot 4 this doubles as the proof that Jackson 3 binds the request record. |
| `OrderControllerTest.postOrderWithException` | `InsufficientIngredientsException` from the service still maps to `400` through `FoodicsChallengeControllerAdvice`. |
| `OrderServiceIntegrationTest.saveOrderNoEmail` | An order below the threshold persists one `Order` and sets `consumedAmountInGrams` on every ingredient, sending no email. |
| `OrderServiceIntegrationTest.saveOrderEmail` | An order crossing 50% persists and sends exactly one email; three ingredients carry a consumed amount. |
| `OrderServiceIntegrationTest.saveOrderEmailOnceWithMultipleOrderRequests` | Two orders past the same threshold still send exactly one email in total. |

`FoodicsCodingChallengeApplicationTests.contextLoads` is the sixth, and its status depends on Open
question 1 in `requirements.md`. Whatever it does on the Boot 3.3.1 baseline it must still do on
Boot 4 — if it was passing it must pass, and if it was failing for want of PostgreSQL it must fail
the same way and no worse.

If a test needs its *assertions* changed to pass, the phase has failed its own premise. Changing an
*import* or an *annotation* is the migration; changing an expected value is a behavioural change
wearing a migration's clothes, and needs to be raised rather than absorbed.

## Manual

0. **Bring up a throwaway PostgreSQL.** No local install, and deliberately **no volume** — the
   container's data lives and dies with it, which is exactly right for a schema that
   `ddl-auto: create-drop` rebuilds on every start. Docker Desktop's engine must be running first;
   the CLI being on `PATH` is not enough (`docker info` fails with a named-pipe error when it is not).

   ```sh
   docker run --rm -d --name foodics-pg \
     -e POSTGRES_USER=foodics -e POSTGRES_PASSWORD=foodics -e POSTGRES_DB=foodics \
     -p 5432:5432 postgres:17-alpine
   ```

   Point `application-dev.yaml` at it — `spring.datasource.username: foodics`,
   `password: foodics` — matching the `application-dev.yaml.example` placeholders. The URL in
   `application.yaml` already targets `localhost:5432/foodics`, so nothing else changes.

   Wait for readiness rather than guessing:
   `docker exec foodics-pg pg_isready -U foodics` → `accepting connections`.

   Tear down at the end with `docker stop foodics-pg`; `--rm` removes the container and its
   anonymous storage with it, so a re-run of these steps always starts from an empty database.

1. **Boot on Java 21 against that container.**
   `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
   Expected: the banner reports Spring Boot 4.0.x, startup logs show the JVM as 21.0.x, and
   `DatabaseInitializer` seeds without error. Confirm the seeding actually landed:

   ```sh
   docker exec foodics-pg psql -U foodics -d foodics \
     -c "SELECT count(*) FROM product;" -c "SELECT count(*) FROM ingredient;"
   ```

   → 2 and 4. A fresh container is what makes this check meaningful: on a reused database the counts
   could be left over from an earlier run rather than written by this one.

2. **An order still succeeds and still depletes stock.**
   ```
   POST http://localhost:8080/order
   Content-Type: application/json

   {"products":[{"productId":1,"quantity":2},{"productId":2,"quantity":2}]}
   ```
   Expected: `200` with body `true`. Then, against the same container:

   ```sh
   docker exec foodics-pg psql -U foodics -d foodics \
     -c "SELECT name, amount_in_grams, consumed_amount_in_grams FROM ingredient ORDER BY name;"
   ```

   — Beef at 300 consumed, Chicken at 300, Cheese at 120, Onion at 80.

3. **An impossible order is still refused.**
   ```
   POST http://localhost:8080/order
   Content-Type: application/json

   {"products":[{"productId":1,"quantity":100}]}
   ```
   Expected: `400` with the plain-text body `Insufficient ingredients for your order`, and the same
   `SELECT` as step 2 returning `consumed_amount_in_grams` unchanged — the rollback still rolls back.

4. **The alert still sends.** With `application-dev.yaml` supplying real Gmail credentials, order
   enough of product 1 to push an ingredient past 50% (`quantity: 26` is what the test fixture uses)
   and confirm one mail arrives at the address in `MERCHANT_EMAIL`. Repeat the same order and confirm
   no second mail — the at-most-once rule survives the upgrade.

5. **Tear down.** `docker stop foodics-pg`. Then re-run step 1 against a freshly started container
   and confirm the counts are 2 and 4 again — proof that `DatabaseInitializer` seeds a genuinely
   empty database rather than relying on what a previous run left behind.

## Merge criteria

- [ ] `mvn clean verify` passes on a Java 21 JDK with no deprecation-removal errors, and the existing
      order, alerting, and rejection tests are green without behavioural change *(the roadmap's Phase 7
      exit criteria, verbatim)*
- [ ] `pom.xml` declares `spring-boot-starter-parent` 4.0.x and `<java.version>21</java.version>`
- [ ] No explicit `<version>` remains on `hibernate-validator` or `postgresql`
- [ ] No source file references `org.springframework.boot.test.mock.mockito` or
      `com.fasterxml.jackson`
- [ ] All manual steps 0–5 produce the expected results, against a `--rm`, volume-less
      `postgres:17-alpine` container rather than an installed PostgreSQL
- [ ] `README.md` and `specs/tech-stack.md` name Java 21 and Spring Boot 4; Phase 7's roadmap items
      are ticked with the landed version recorded
- [ ] No commented-out dependencies, no `-SNAPSHOT` or milestone versions, no temporary
      `System.out.println` left in the migration commits
- [ ] Open questions 1–3 in `requirements.md` are each answered in the branch's commit history, not
      left open

## Rollback

`git revert` the branch's merge commit, or reset `pom.xml` to the Boot 3.3.1 / Java 17 parent and
revert the test and request-model commits with it — the source changes are only valid against Boot 4
and cannot be kept independently. Nothing outside the repository changes: no schema migration is
applied (`ddl-auto: create-drop` rebuilds on every start) and no configuration or credential moves,
so reverting the code is the whole rollback.
