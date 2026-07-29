# Tech Stack

## Current stack

What `pom.xml` builds today, matching roadmap phases 1–6.

| Layer | Technology | Rationale |
| --- | --- | --- |
| Language / runtime | Java 17 | LTS release; `java.version` is pinned to 17 in `pom.xml` |
| Framework | Spring Boot 3.3.1 | Supplies the web layer, dependency injection, transactions, and mail in one starter set |
| Web | `spring-boot-starter-web` | REST controller for the single `POST /order` endpoint |
| Persistence | Spring Data JPA / Hibernate | Entity mapping and repositories without hand-written SQL |
| Database | PostgreSQL 42.7.3 driver | Relational store; transactional guarantees are what make all-or-nothing order handling possible |
| Validation | Hibernate Validator 8.0.1.Final | Bean validation on incoming request models |
| Email | `spring-boot-starter-mail` over Google SMTP | Sends the low-stock alert; `smtp.gmail.com:587` with STARTTLS |
| Testing | `spring-boot-starter-test` (JUnit 5, MockMvc, Mockito) | Controller tests and service-level integration tests |
| Test database | H2, in-memory | Integration tests run without a live PostgreSQL instance |
| Build | Maven, via the bundled `mvnw` wrapper | Reproducible build without a system-wide Maven install |

## Target stack

Planned, tracked as roadmap phases 7–13. Nothing below is in `pom.xml` yet.

| Layer | Technology | Rationale |
| --- | --- | --- |
| Language / runtime | Java 21 | Current LTS; replaces the Java 17 pin. Records, pattern matching, and virtual threads become available |
| Framework | Spring Boot 4.0.x | Current generation, on Spring Framework 7 and Jakarta EE 11; keeps the project on a supported line and lets the parent manage validator and driver versions |
| Schema migration | Liquibase | Versioned, reviewable schema changes; replaces `ddl-auto: create-drop` with `validate` so the database survives a restart and drift is caught at boot |
| Authentication | Spring Security + `oauth2-resource-server` (Nimbus JOSE) | Stateless JWT bearer authentication. Uses Spring's own JWT decoding rather than a third-party library, so verification, clock skew, and claim handling are framework-maintained |
| Password hashing | BCrypt via `PasswordEncoder` | Spring Security default; adaptive cost, per-hash salt |
| Authorization | Role-based rules plus ownership checks | Three roles — `CUSTOMER`, `RESTAURANT_OWNER`, `SUPPLIER`. Role alone is not enough: an owner or supplier may only reach their own rows |
| Test security | `spring-security-test` | `@WithMockUser` and bearer-token request post-processors, so the existing MockMvc tests can assert `401`/`403` alongside the happy path |
| Email delivery | Resend (`com.resend:resend-java`), behind an `EmailSender` port | Free tier of 3,000/month covers a challenge project with room to spare, and removes the personal Gmail app password. SMTP stays as a profile-selected fallback |
| Local / test mail | Mailpit locally, GreenMail in tests | Nothing leaves the machine during development, and the suite runs with no network and no API key |

## Architecture

A three-layer arrangement, dependencies pointing inward from the edge:

- **Controller** — `OrderController` exposes `POST /order`. `FoodicsChallengeControllerAdvice` translates
  `InsufficientIngredientsException` into a `400` with a plain-text message.
- **Service** — `OrderService` orchestrates a request: it resolves the ordered products, delegates stock
  depletion to `IngredientService`, then builds and saves the `Order` with its `OrderDetail` lines.
  `IngredientService` owns the stock rules and the 50% threshold detection. `EmailService` wraps
  `JavaMailSender`.
- **Repository** — Spring Data interfaces over `Product`, `Ingredient`, `ProductIngredient`, and `Order`.

`Product` and `Ingredient` are joined many-to-many through `ProductIngredient`, which carries the recipe
amount in grams. `Order` and `Product` are joined through `OrderDetail`, which carries the ordered quantity.

### Planned additions

The three layers stay as they are; the planned work adds actors around them and a filter in front of them.

- **Security filter** — a stateless `SecurityFilterChain` ahead of the controllers. `POST /auth/login`
  issues a signed JWT; every other request is authenticated by decoding the bearer token, with no session
  and no CSRF. Authorization is expressed on the endpoints, and ownership is checked in the services that
  hold the data.
- **New entities** — `Restaurant` owns `Product` (which gains a `price`); `Supplier` supplies `Ingredient`
  and carries the notification address; `User` carries an email, a hashed password, a `Role`, and a link to
  the restaurant or supplier it acts for. `Order` gains the customer who placed it.
- **New read model** — `MenuService` answers `GET /restaurants/{id}/menu` with response projections, not
  entities, so recipe amounts and stock levels stay internal.

## Decisions and trade-offs

- **Consumed amount rather than remaining amount.** `Ingredient` stores a fixed `amountInGrams` (the
  original stock) plus a growing `consumedAmountInGrams`. Comparing the two is what makes the 50% crossing
  detectable — the service can tell "was below the threshold before, is above it now" and so send the alert
  exactly once. Storing only a remaining balance would lose that before/after distinction.
- **Transaction boundary as the atomicity mechanism.** `OrderService` and `IngredientService` are both
  `@Transactional`. `InsufficientIngredientsException` thrown mid-way through the ingredient loop rolls the
  whole transaction back, so a partially consumed order cannot be persisted. No manual compensation logic.
- **Asynchronous email.** `EmailService.sendEmail` is `@Async` so SMTP latency or failure does not delay or
  fail the order response. The trade-off is that a send failure is not surfaced to the caller.
- **Seeded reference data instead of CRUD endpoints.** `DatabaseInitializer` is a `CommandLineRunner` that
  populates two products and four ingredients at startup, standing in for a DBA-managed catalogue. This
  keeps the scope on the ordering workflow. It pairs with `ddl-auto: create-drop`, so the schema and the
  seed data are rebuilt on every run.
- **H2 for tests over a containerised PostgreSQL.** Faster and dependency-free for a challenge submission,
  at the cost of not exercising PostgreSQL-specific behaviour.

### Planned

- **Liquibase over Flyway.** Either would do. Liquibase's database-independent changelog format lets the
  same changesets run against PostgreSQL and the H2 test database, which Flyway's raw SQL would not without
  a second set of scripts. The cost is a more verbose changelog and a format that is less obvious to read
  than plain SQL.
- **Baseline changeset rather than a generated snapshot.** The first changelog reproduces the schema
  Hibernate currently generates, so the switch to `ddl-auto: validate` is a no-op on an existing database.
  The seed data moves into a contexted changeset, which also ends the "everything is lost on shutdown"
  behaviour of `create-drop`.
- **Stateless JWT over server-side sessions.** A bearer token means no session store and no sticky routing,
  which suits an API consumed by point-of-sale clients. The trade-offs are the usual ones: a token cannot be
  revoked before it expires, so lifetimes stay short, and there is no refresh-token flow planned in this
  scope.
- **Spring Security's resource server over a JWT library.** Signature verification, expiry, and claim
  extraction are handled by framework code rather than by hand-rolled parsing with `jjwt`. Only token
  *issuance* at login is ours.
- **Role plus ownership, not role alone.** `RESTAURANT_OWNER` says what kind of thing a caller may do;
  it must not say *whose* data they may do it to. Every owner- or supplier-scoped read and write also
  checks that the row belongs to the authenticated principal, or one restaurant could read and alter
  another's menu and stock.
- **Per-ingredient supplier as the alert recipient.** The hard-coded `MERCHANT_EMAIL` becomes a lookup
  through `Ingredient.supplier`, and alerts are grouped per supplier so nobody is told about ingredients
  they do not supply. The at-most-one-alert-per-ingredient rule and the `@Async` send are unchanged.
- **Resend's free tier over SES, Postmark, or staying on Gmail.** The July 2026 survey put Amazon SES
  cheapest at scale ($0.10/1,000) and Postmark strongest on deliverability (from ~$15/mo), but this is a
  coding challenge: it sends a handful of alerts, so cost dominates and both of those lose to a free tier.
  Gmail SMTP is also free, and stays as a profile-selected fallback, but it caps out around 500 sends a day
  and requires someone's personal app password — which is the actual reason to move. The transport goes
  behind an `EmailSender` port so swapping it later is one class.
- **Fire-and-forget email retained, knowingly.** The reliable alternative is a transactional outbox: write
  the alert row inside the order's transaction, relay it on a schedule, retry on failure. It closes a real
  dual-write hole — today the order can commit while the alert is lost to an SMTP failure, and the
  threshold never fires again for that ingredient. It is not being adopted: a lost alert in a challenge
  project costs nothing, and the outbox costs a table, a relay job, and idempotency handling. This is a
  scope decision, not a claim that `@Async` is safe.
- **Availability computed, not stored, on the menu.** A menu item's availability is derived from current
  stock at read time rather than kept as a flag, so it cannot drift from the stock the order path checks.
  The cost is that the menu query must fetch recipes and ingredient stock, which is why it is one query
  with joins rather than a lookup per product.

## Constraints

- Java 17 or later is required to build and run.
- A reachable PostgreSQL instance is required at `localhost:5432`, database `foodics`.
- Credentials are not committed. `application-dev.yaml.example` is the template; a local
  `application-dev.yaml` must supply the datasource username and password and the Gmail address and app
  password. The application runs under the `dev` profile:
  `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
- Google SMTP requires an app password, not the account password.
- `ddl-auto: create-drop` means all data, including orders, is discarded on shutdown. This is a
  development-only setting.
- The merchant's alert address is currently a compile-time constant in `IngredientService`
  (`MERCHANT_EMAIL`), not configuration. Phase 12 replaces it with the ingredient's supplier.

### Planned

- A Java 21 JDK becomes the minimum to build and run, replacing Java 17.
- Spring Boot 4 restructured a number of modules, so the upgrade is not a version-number change alone —
  starter coordinates and removed APIs have to be reconciled against the 4.0 migration guide, and the
  explicit `hibernate-validator` pin should come out so the parent manages it.
- Once Liquibase owns the schema, entity changes without a matching changeset will fail startup under
  `ddl-auto: validate`. That is the point, but it means every model change is a two-file change.
- The JWT signing key and token lifetime are configuration, not code, and follow the same rule as the
  SMTP credentials: supplied through the uncommitted `application-dev.yaml`, never checked in.
- `Product` has no `price` and there is no `Restaurant` entity today, so the menu endpoint cannot be built
  before Phase 9 introduces both.
