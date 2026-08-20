# Phase 12 — Restaurants, suppliers, and accounts

## Context

Today the catalogue is global and anonymous: `Product` (`src/main/java/com/foodics/challenge/model/entity/Product.java`)
and `Ingredient` (`.../entity/Ingredient.java`) belong to nobody, `POST /order` resolves any
`productId` against the whole catalogue via `ProductRepository.findByIdIn` (called from
`ProductService.getAllProductsByIdIn`, `src/main/java/com/foodics/challenge/service/ProductService.java`),
and a requested id that doesn't exist is silently dropped — `findByIdIn` just returns fewer rows
than asked for, and `OrderService.order` (`.../service/OrderService.java`) never notices. Low-stock
alerts go to a single hard-coded `MERCHANT_EMAIL` constant in `IngredientService`
(`.../service/IngredientService.java:21`). The schema is Liquibase-managed as of Phase 9
(`src/main/resources/db/changelog/`), the response envelope and numbered `ErrorCode`s are in place
as of Phase 10 (`.../exception/ErrorCode.java`, `.../controlleradvice/FoodicsChallengeControllerAdvice.java`),
and there is no security dependency in `pom.xml` yet — `spring-boot-starter-security` is Phase 13's
addition, not this one's.

This phase gives the domain the actors the mission needs before authentication (Phase 13) and
authorization (Phase 14) can mean anything: a restaurant owns a menu, a supplier owns ingredients,
and a user account exists per role. It also closes the silent-drop bug by making `POST /order`
resolve products against one named restaurant's menu and reject unknown or foreign product ids
instead of quietly proceeding with a subset.

## Scope

- `Restaurant` entity; `Product` gains a `restaurant` reference and a `price`.
- `Supplier` entity with a contact email; `Ingredient` gains a `supplier` reference. The low-stock
  alert in `IngredientService` is addressed to the ingredient's supplier instead of `MERCHANT_EMAIL`.
- `User` entity: unique email, hashed password, `Role` (`CUSTOMER`, `RESTAURANT_OWNER`, `SUPPLIER`),
  and an ownership link — a `RESTAURANT_OWNER` to their `Restaurant`, a `SUPPLIER` to their
  `Supplier` — populated only for the role it applies to.
- `OrderRequest` gains a required `restaurantId`; `OrderService.order` resolves the requested
  `productId`s against that restaurant's own products, not the global catalogue.
- `ProductNotFoundException`, thrown with the missing/foreign product id(s) when a requested id
  doesn't resolve to a product on the named restaurant's menu, mapped to its own numbered
  `ErrorCode` through `FoodicsChallengeControllerAdvice`.
- Liquibase changesets for every new table and column, plus a backfill changeset so the seeded
  Burger/Chicken Burger catalogue ends up owned by one restaurant, all four ingredients get a
  supplier, and one seeded user exists per role.

## Out of scope

- Authentication (`POST /auth/login`, JWT issuance, `SecurityFilterChain`) — Phase 13.
- Authorization / ownership enforcement on any endpoint — Phase 14. This phase creates the
  ownership *links* in the data model; nothing yet checks them at request time.
- Routing low-stock alerts per-supplier with grouping — Phase 15. This phase only repoints the
  existing single-alert flow at `Ingredient.supplier`'s email instead of the constant; the
  at-most-one-email-per-order behavior from Phase 5 is unchanged.
- The menu endpoint (`GET /restaurants/{id}/menu`) — Phase 16.
- User registration, password reset, or any endpoint that creates/edits a `User`, `Restaurant`, or
  `Supplier` — all three are seeded via Liquibase, matching the "no CRUD endpoints" scope in
  `specs/mission.md`.
- Multiple restaurants or suppliers in seed data — one of each is enough to prove the model and the
  scoping behavior; more is Phase 14/16 territory when ownership actually gets exercised.

## Decisions

- **Password hashing via `spring-security-crypto`, not the full `spring-boot-starter-security`
  starter.** The roadmap asks for a "hashed password" column on `User` in this phase, but the
  `SecurityFilterChain`, `UserDetailsService`, and login endpoint are explicitly Phase 13's. Adding
  the crypto module alone gives `BCryptPasswordEncoder` for hashing the seeded password without
  pulling in a filter chain this phase has nowhere to wire up yet.
- **Seeded password hash computed offline, not encoded at startup.** The Liquibase seed changeset
  inserts a literal BCrypt hash string for the one seeded user per role, the same way `002-seed-catalogue-data.yaml`
  inserts literal data today. There's no `CommandLineRunner` or migration-time Java code that would
  need `PasswordEncoder` wired into the changelog process.
- **`ProductNotFoundException` scoped by restaurant, not just existence.** A `productId` that
  exists but belongs to a different restaurant is treated identically to one that doesn't exist at
  all — both come back as "not found" naming the id, per the roadmap's exit criteria. The service
  resolves products with a single `findByIdInAndRestaurantId` query and diffs the requested ids
  against what came back, rather than fetching by id first and checking `product.getRestaurant()`
  per row.
- **New `ErrorCode` appended at the end of the enum, after `UNCLASSIFIED_ERROR`.** `ErrorCode`
  derives its wire code from `ordinal() + 1`, and `OrderControllerTest` already hardcodes `E0001`,
  `E0005`, `E0006` for existing errors. Inserting `PRODUCT_NOT_FOUND` anywhere but the end would
  silently renumber every code after it.
- **Ownership columns on `User` (`restaurant_id`, `supplier_id`) are both nullable, not a subtype
  per role.** A `CUSTOMER` has neither set, a `RESTAURANT_OWNER` has `restaurant_id` set, a
  `SUPPLIER` has `supplier_id` set. A `CHECK` constraint enforcing "exactly the right column for the
  role" is left out — Phase 14 is where ownership actually gets read and enforced, and a malformed
  seed row would already have to be a Liquibase-authoring mistake, not user input, since there's no
  write endpoint yet.
- **`price` as `NUMERIC(10,2)` / `BigDecimal`.** Money as a floating type would misrepresent cents;
  the mission doesn't ask for multi-currency, so no currency column is added.
- **`Product.restaurant` and `Ingredient.supplier` are `NOT NULL`.** Nothing in scope creates a
  product or ingredient without an owner, so there's no legitimate orphan state to allow for.

## Open questions

- None. The roadmap's exit criteria and the mission's "in scope"/"planned" split leave no
  ambiguity about what this phase does and doesn't touch.
