# Roadmap

Phases 1–6 are a record of how the delivered solution was built; each was a working increment, and they map
to the commit history from 2024-07-09 to 2024-07-13. Phases 7 onward are planned and not yet started.

# Delivered

## Phase 1 — Project skeleton

**Goal.** A Spring Boot application that starts.

- [x] Maven project with the Spring Boot 3.3.1 parent, Java 17, and the wrapper scripts
- [x] `web`, `data-jpa`, PostgreSQL driver, and validator dependencies
- [x] `FoodicsCodingChallengeApplication` entry point
- [x] `application.yaml` with the datasource and JPA settings

**Exit criteria.** `mvn spring-boot:run` boots and connects to a local PostgreSQL database.

## Phase 2 — Domain model

**Goal.** A schema that can express recipes and orders.

*Depends on Phase 1.*

- [x] `Product` and `Ingredient` entities, the latter holding `amountInGrams` as its total stock
- [x] `ProductIngredient` join entity with a composite `ProductIngredientKey`, carrying the per-product
      recipe amount in grams
- [x] `Order` and `OrderDetail` with a composite `OrderDetailsKey`, carrying the ordered quantity
- [x] Spring Data repositories for each aggregate

**Exit criteria.** Hibernate generates the schema on startup, and a product can be related to several
ingredients with distinct amounts.

## Phase 3 — Seed data

**Goal.** A populated catalogue to order against, standing in for DBA-managed reference data.

*Depends on Phase 2.*

- [x] `DatabaseInitializer` as a `CommandLineRunner`
- [x] Two products — Burger and Chicken Burger
- [x] Four ingredients — Beef 20000g, Chicken 20000g, Cheese 5000g, Onion 1000g
- [x] Recipes wiring each product to its ingredients (150g protein, 30g cheese, 20g onion)

**Exit criteria.** A fresh start leaves the database ready to accept an order with no manual setup.

## Phase 4 — Ordering workflow

**Goal.** An order can be placed and depletes stock.

*Depends on Phase 3.*

- [x] `OrderController` exposing `POST /order`, with `OrderRequest` / `ProductRequest` payloads
- [x] `OrderService` resolving ordered products and persisting the order with its line items
- [x] `IngredientService.updateIngredientsStock` computing consumption as `quantity × amountInGrams`
- [x] `consumed_amount_in_grams` added to `Ingredient` to track cumulative depletion
- [x] `@Transactional` boundaries making the order all-or-nothing

**Exit criteria.** A valid order returns `200`, is persisted, and every affected ingredient's consumed
amount increases correctly.

## Phase 5 — Alerting and rejection

**Goal.** The merchant is warned before stock runs out, and impossible orders are refused.

*Depends on Phase 4.*

- [x] 50%-threshold detection comparing consumed amount before and after the order, so each ingredient
      triggers at most one alert
- [x] `EmailService` over Google SMTP, `@Async` so delivery never blocks the response
- [x] Message composition listing one or several low ingredients in readable form
- [x] `InsufficientIngredientsException` thrown when an order would exceed available stock
- [x] `FoodicsChallengeControllerAdvice` mapping it to `400` with "Insufficient ingredients for your order"
- [x] `updateIngredientsStock` refactored after the logic settled

**Exit criteria.** Crossing 50% sends exactly one email; exceeding 100% rejects the order and rolls back
every stock change.

## Phase 6 — Tests and documentation

**Goal.** The behaviour is verified and the solution can be run by someone else.

*Depends on Phase 5.*

- [x] `OrderControllerTest` covering the endpoint contract
- [x] `OrderServiceIntegrationTest` covering depletion, threshold alerting, and rejection
- [x] H2 and `application-test.yaml` so tests run without PostgreSQL
- [x] `OrderRequestUtils` test fixtures
- [x] README documenting the stack, the layered architecture, the assumptions, and the run instructions
- [x] `application-dev.yaml.example` template so credentials stay uncommitted

**Exit criteria.** `mvn test` passes, and the README alone is enough to run the application locally.

# Planned

## Phase 7 — Java 21 and Spring Boot 4

**Goal.** The application builds and runs on the current runtime and framework generation.

*Depends on Phase 6.*

- [ ] `java.version` raised from 17 to 21 in `pom.xml`; toolchain and CI (if any) aligned
- [ ] `spring-boot-starter-parent` moved from 3.3.1 to 4.0.x
- [ ] Starter artifact names and removed APIs reconciled against the Spring Boot 4.0 migration guide —
      Boot 4 restructured several modules, so assume nothing carries over untouched
- [ ] Explicit `hibernate-validator` 8.0.1.Final pin dropped, letting the parent manage the version
- [ ] Java 21 language features adopted only where they clarify existing code (records for request models,
      pattern matching in the advice), not as a rewrite

**Exit criteria.** `mvn clean verify` passes on a Java 21 JDK with no deprecation-removal errors, and the
existing order, alerting, and rejection tests are green without behavioural change.

## Phase 8 — Environment-variable configuration

**Goal.** The application is configured entirely through environment variables, with no per-profile YAML
file and nothing environment-specific committed to the repository.

*Depends on Phase 7.*

- [x] `application-dev.yaml.example` and the gitignored `application-dev.yaml` it templates removed
- [x] `application-test.yaml` removed; the `test` profile stops owning its own YAML file
- [x] `application.yaml` made the single Spring configuration file, with every environment-specific value —
      datasource URL, datasource credentials, mail host/credentials — read from an environment variable via
      `${VAR_NAME}` placeholders; local-friendly values (e.g. the H2 URL for tests) get an inline default,
      credentials do not
- [x] Test configuration supplied as environment variables for the test run (Surefire/Failsafe `<environmentVariables>`
      in `pom.xml`, or equivalent), so `mvn test` needs no YAML profile to run against H2
- [x] `spring.profiles.active` usage reconsidered now that dev and test no longer carry their own YAML —
      dropped if nothing remains profile-specific, kept only if it still selects real behavioural differences
- [x] `.gitignore` entry for `application-dev.yaml` removed since the file no longer exists
- [x] README's configuration section rewritten to list the required environment variables and how to set
      them (shell export, `.env` file, IDE run configuration) instead of pointing at copying an example YAML

**Exit criteria.** No `application-dev.yaml.example` or profile-specific YAML remains under
`src/main/resources` or `src/test/resources`; `mvn clean verify` passes and the application starts using
only environment variables for datasource and mail configuration; the README alone is enough to configure
and run the app in a fresh environment.

## Phase 9 — Liquibase-managed schema

**Goal.** The schema is versioned and reviewable instead of being inferred from entities at startup.

*Depends on Phase 8.*

- [x] `liquibase-core` added and `spring.liquibase` configured
- [x] `db/changelog/db.changelog-master.yaml` plus one changelog per change, starting with a baseline
      changeset capturing today's tables, sequences, and constraints
- [x] `ddl-auto` changed from `create-drop` to `validate`, so Hibernate checks the migrated schema rather
      than creating it
- [x] Seed data moved from `DatabaseInitializer` into a changeset with a context, so reference data is
      versioned too and no longer disappears on shutdown
- [x] Tests run the same changelog against H2, so migrations are exercised on every build

**Exit criteria.** A fresh database is built entirely by Liquibase, a second start is a no-op, `validate`
finds no drift, and `mvn test` passes against the migrated H2 schema.

## Phase 10 — Global response envelope

**Goal.** Every response — success or error — carries a consistent envelope, and controllers stop
constructing `ResponseEntity` by hand.

*Depends on Phase 9.*

- [x] Controller methods return their domain object directly (`OrderController.order` returns `Boolean`,
      not `ResponseEntity<Boolean>`); no `ResponseEntity` remains anywhere under `controller` or
      `controlleradvice`
- [x] `FoodicsChallengeControllerAdvice` becomes `@RestControllerAdvice`; its handlers use `@ResponseStatus`
      and return the error payload directly instead of building a `ResponseEntity`
- [x] Every exception the API can currently produce gets its own handler and a stable numbered code —
      `InsufficientIngredientsException`, bean-validation failures, a malformed request body, a
      type-mismatched parameter, an unmapped route, an unsupported HTTP method, and an uncaught
      exception — numbered `E0001`…`E000N` in the order they're declared
- [x] A global `ResponseBodyAdvice<Object>` wraps every response body: success gets `code: "S000"` and the
      original return value under `data`; an error gets its handler's `E000N` code and its message under
      `data`; both carry the HTTP status reason phrase (e.g. `"OK"`, `"Bad Request"`) alongside `code`
- [x] Existing controller/integration tests updated for the new envelope shape; new tests cover a success
      response and at least one of each numbered error

**Exit criteria.** `POST /order` returns `{"code":"S000","status":"OK","data":true}` on success; each
mapped error returns its own `E000N` code with the matching HTTP status; `mvn test` passes.

## Phase 11 — Paginated response support

**Goal.** The response envelope can carry pagination metadata as a sibling of `data`, ready for any future
list endpoint, without disturbing the shape of non-paginated responses.

*Depends on Phase 10.*

- [ ] `PaginationInfo` record (`page`, `size`, `totalElements`, `totalPages`) with a `from(Page<?>)` factory
- [ ] `PagedResult<T>` record (`items`, `paginationInfo`) — the marker a controller returns for a paginated
      list, the same way `ApiError` marks an error
- [ ] `ApiResponse` gains a `paginationInfo` field, omitted from the JSON body
      (`@JsonInclude(Include.NON_NULL)`) when absent, so every existing non-paginated response is unchanged
- [ ] `ApiResponseBodyAdvice` recognizes `PagedResult`, unwrapping `items` into `data` and
      `paginationInfo` into the sibling field
- [ ] Tests prove the shape both ways: a `PagedResult` body serializes with `data` as the item list and a
      populated `paginationInfo`; a plain (non-paged) body serializes with `paginationInfo` absent
      entirely, not `null` — exercised directly against `ApiResponseBodyAdvice`/`ApiResponse`, since no
      list endpoint exists yet to drive it end-to-end (the catalogue stays seeded-only per the mission's
      "no CRUD endpoints" scope)

**Exit criteria.** `mvn test` passes; a `PagedResult` returned from a controller serializes as
`{"code":"S000","status":"...","data":[...],"paginationInfo":{...}}`, while every other response keeps
today's three-field shape.

## Phase 12 — Restaurants, suppliers, and accounts

**Goal.** The domain knows who owns a menu, who supplies an ingredient, and who is ordering.

*Depends on Phase 11.*

- [ ] `Restaurant` entity; `Product` gains a `restaurant` reference and a `price`, so a product belongs to
      exactly one menu
- [ ] `Supplier` entity holding a contact email; `Ingredient` gains a `supplier` reference, replacing the
      `MERCHANT_EMAIL` constant in `IngredientService`
- [ ] `User` entity with a unique email, a hashed password, and a `Role` — `CUSTOMER`, `RESTAURANT_OWNER`,
      or `SUPPLIER`
- [ ] Ownership links: a `RESTAURANT_OWNER` user to their `Restaurant`, a `SUPPLIER` user to their `Supplier`
- [ ] Liquibase changesets for every new table and column, plus backfill for the seeded catalogue

**Exit criteria.** The seeded data resolves to one restaurant owning both products, suppliers attached to all
four ingredients, and one user per role; existing order tests still pass.

## Phase 13 — JWT authentication

**Goal.** A caller can prove who they are.

*Depends on Phase 12.*

- [ ] `spring-boot-starter-security` and `spring-boot-starter-oauth2-resource-server` added
- [ ] `POST /auth/login` taking email and password, returning a signed JWT carrying the subject and role
- [ ] `PasswordEncoder` (BCrypt) and a `UserDetailsService` backed by `User`
- [ ] `SecurityFilterChain` as a stateless resource server: no sessions, no CSRF, JWT decoded per request
- [ ] Signing key and token lifetime as configuration, kept out of the repository like the SMTP credentials
- [ ] `401` for a missing, malformed, or expired token, mapped through
      `FoodicsChallengeControllerAdvice` for a consistent error body

**Exit criteria.** Valid credentials return a token that a protected endpoint accepts; a wrong password, a
tampered signature, and an expired token are each rejected with `401`, covered by tests.

## Phase 14 — Role-based authorization

**Goal.** Every endpoint is reachable only by the roles that should reach it, and only for their own data.

*Depends on Phase 13.*

- [ ] Endpoints locked down by default — anything not explicitly permitted requires authentication
- [ ] `POST /order` restricted to `CUSTOMER`, with the order attributed to the authenticated user
- [ ] Owner-scoped reads restricted to `RESTAURANT_OWNER` — their own restaurant's menu including
      unavailable items, and their own ingredient stock levels
- [ ] Supplier-scoped reads restricted to `SUPPLIER` — the stock levels of the ingredients they supply,
      and nothing else
- [ ] Ownership checks beyond the role: an owner acts only on their own restaurant, a supplier reads only
      their own ingredients — a role alone must not grant access to another actor's rows
- [ ] `403` for an authenticated caller with the wrong role or the wrong owner, distinct from `401`
- [ ] Request validation kept at the edge: `@Valid` on every payload, `400` with field-level messages

**Exit criteria.** A test per role proves both the allowed call succeeds and the forbidden one returns `403`,
including the cross-tenant case where the role is right and the owner is wrong.

## Phase 15 — Supplier low-stock notifications

**Goal.** The alert reaches the supplier who can actually restock the ingredient.

*Depends on Phase 14.*

- [ ] 50%-threshold detection reused, but the recipient resolved from `Ingredient.supplier` instead of the
      hard-coded constant
- [ ] Ingredients crossing the threshold in one order grouped per supplier, so each supplier receives one
      email listing their own ingredients only
- [ ] Message body naming the ingredient, the restaurant, and the remaining amount
- [ ] Alerting still `@Async` and still unable to fail the order; a send failure logged, not swallowed silently
- [ ] The at-most-one-alert-per-ingredient guarantee kept intact

**Exit criteria.** An order depleting ingredients from two suppliers sends exactly two emails, each listing
only that supplier's ingredients, and a second order past the same threshold sends none.

## Phase 16 — Restaurant menu endpoint

**Goal.** A client can read what a restaurant sells before ordering it.

*Depends on Phase 15.*

- [ ] `GET /restaurants/{restaurantId}/menu` returning the restaurant's products with id, name, and price
- [ ] Availability per item, derived from whether the recipe can still be covered by remaining ingredient
      stock, so a customer is not offered something the order would reject
- [ ] `MenuResponse` / `MenuItemResponse` projections — entities are not serialised directly, and recipes
      and stock figures are not exposed to customers
- [ ] A single query per menu, fetching products with their `ProductIngredient` rows; the N+1 across
      products and ingredients is the failure mode to watch here
- [ ] `404` for an unknown restaurant; authenticated access for any role
- [ ] Controller and integration tests covering an available item, an unavailable one, and the unknown
      restaurant

**Exit criteria.** The endpoint returns both seeded products for the seeded restaurant, an item flips to
unavailable once its ingredients cannot cover one unit, and the response is produced without a query per
product.

## Phase 17 — Email delivery off a personal account

**Goal.** Alerts leave the application without anyone lending it a Gmail app password.

*Depends on Phase 15.*

The alternatives were surveyed in July 2026: Amazon SES ($0.10/1,000, cheapest at scale, most setup),
Postmark (from ~$15/mo, best deliverability), Resend (3,000/mo free, ~$20 for 50k), Brevo (300/day,
~9,000/mo free forever, offers a plain SMTP relay alongside its API), Mailgun (~$15–90/mo, EU/US data
residency), and SendGrid (~$19.95–89.95/mo, no free tier since May 2025). This is a coding challenge
rather than a running business, so cost outweighs deliverability and scale: **Resend's free tier** is the
primary choice for the API adapter, and **Brevo's free SMTP relay** replaces Gmail as the fallback — both
free indefinitely at this project's volume — with the paid options staying on the record only in case the
project ever becomes real.

- [ ] `EmailSender` port extracted from `EmailService`, so the transport is one implementation behind an
      interface rather than `JavaMailSender` reaching through the service
- [ ] Resend adapter over `com.resend:resend-java`, sending from a verified domain or Resend's test
      sender, with the API key supplied as configuration and kept uncommitted like the SMTP credentials
- [ ] SMTP adapter kept as the fallback, selected by profile, but repointed from Gmail to Brevo's free SMTP
      relay (300 emails/day, no card required) — so even the fallback path no longer depends on anyone's
      personal Gmail app password; host, port, and the Brevo SMTP credentials become configuration
- [ ] Local development pointed at a mail catcher (Mailpit) so nothing leaves the machine
- [ ] Tests pointed at an in-memory SMTP server (GreenMail) or a stubbed `EmailSender`, so the suite
      neither reaches the network nor needs an API key
- [ ] `MERCHANT_EMAIL`'s replacement from Phase 15 unaffected; recipients still come from the supplier

**Deliberately not in this phase.** Delivery stays `@Async` fire-and-forget. No outbox table, no retry, no
broker. A send that fails is logged and lost, and that is accepted: the surveyed alternative — writing the
alert in the order's transaction and relaying it on a schedule — buys durability this project does not need
and costs a table, a relay job, and idempotency handling. Revisit only if the service ever takes real
orders.

**Exit criteria.** An alert sends through Resend with no Gmail credentials configured, the SMTP profile
sends the same message unchanged through Brevo's relay instead of Gmail, and `mvn test` passes with no
network access and no API key present.
