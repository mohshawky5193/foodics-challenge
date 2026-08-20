# Phase 12 — Restaurants, suppliers, and accounts — Validation

## Automated

- `mvn test` must pass. Covers:
  - `OrderControllerTest` — happy path with `restaurantId`, missing `restaurantId` → `400`/`E0002`,
    unknown `productId` → `404`/`PRODUCT_NOT_FOUND`'s code, foreign-restaurant `productId` → same
    `404`, plus the existing insufficient-ingredients/method-not-allowed/route-not-found cases
    unchanged (`E0001`, `E0005`, `E0006` must still resolve to the same codes)
  - `OrderServiceIntegrationTest` — stock depletion and 50%-threshold alerting still correct once
    product resolution is scoped by restaurant
  - New `IngredientService` test — alert recipient is `Ingredient.supplier.email`, not a constant
  - New seed-data test — one restaurant owns both seeded products, one supplier is attached to all
    four seeded ingredients, exactly one `app_user` exists per `Role`

## Manual

Run against a fresh Liquibase-migrated database (`mvn spring-boot:run`, seed context applied):

1. `POST /order` with a valid `restaurantId` and both seeded product ids:
   ```json
   {"restaurantId": 1, "products": [{"productId": 1, "quantity": 2}]}
   ```
   Expect `200` with `{"code":"S000","status":"OK","data":true}`.

2. `POST /order` omitting `restaurantId`:
   ```json
   {"products": [{"productId": 1, "quantity": 2}]}
   ```
   Expect `400` with `code: "E0002"`.

3. `POST /order` naming a `productId` that doesn't exist:
   ```json
   {"restaurantId": 1, "products": [{"productId": 999, "quantity": 1}]}
   ```
   Expect `404` with the new `PRODUCT_NOT_FOUND` code and a message naming `999`.

4. `POST /order` naming a `restaurantId` that isn't the seeded one, with a seeded `productId`:
   ```json
   {"restaurantId": 999, "products": [{"productId": 1, "quantity": 1}]}
   ```
   Expect the same `404`/`PRODUCT_NOT_FOUND` — the product exists but not on that restaurant's menu.

5. Push an order past the 50% threshold on an ingredient and confirm the alert email's recipient is
   the seeded supplier's address, not `mohcufe@gmail.com`.

## Merge criteria

- [ ] The seeded data resolves to one restaurant owning both products, suppliers attached to all
      four ingredients, and one user per role (roadmap exit criteria)
- [ ] An order naming an unknown `productId` (or one that exists but belongs to a different
      restaurant) is rejected with `ProductNotFoundException` instead of silently dropping it
      (roadmap exit criteria)
- [ ] Existing order tests still pass (roadmap exit criteria)
- [ ] `mvn test` green
- [ ] `db.changelog-master.yaml` includes both new changesets and a fresh database migrates cleanly
- [ ] `specs/roadmap.md` Phase 12 checkboxes ticked once the above is true
- [ ] No leftover reference to `MERCHANT_EMAIL` or the old `findByIdIn`/`getAllProductsByIdIn` call
      path

## Rollback

Revert the phase's commit(s) and roll back the two new Liquibase changesets (`liquibase rollback` to
before `003-restaurants-suppliers-accounts-schema.yaml`, or drop the new tables/columns manually in a
throwaway environment) — the prior global, unscoped `POST /order` behavior returns unchanged since no
earlier phase depended on the new columns.
