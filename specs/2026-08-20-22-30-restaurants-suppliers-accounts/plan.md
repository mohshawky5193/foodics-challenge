# Phase 12 — Restaurants, suppliers, and accounts — Plan

## 1. Entities and repositories
- [x] `Restaurant` entity (`model/entity/Restaurant.java`): `id` (sequence `restaurant_id_seq`), `name`,
      `@OneToMany(mappedBy = "restaurant")` `products`
- [x] `Supplier` entity (`model/entity/Supplier.java`): `id` (sequence `supplier_id_seq`), `name`,
      `email`, `@OneToMany(mappedBy = "supplier")` `ingredients`
- [x] `Role` enum (`model/entity/Role.java`): `CUSTOMER`, `RESTAURANT_OWNER`, `SUPPLIER`
- [x] `User` entity (`model/entity/User.java`): `id` (sequence `user_id_seq`), `email`, `passwordHash`
      (column `PASSWORD_HASH`), `role` (`@Enumerated(EnumType.STRING)`), nullable `restaurant`
      (`@ManyToOne`, column `RESTAURANT_ID`), nullable `supplier` (`@ManyToOne`, column `SUPPLIER_ID`)
- [x] `Product` (`model/entity/Product.java`) gains `restaurant` (`@ManyToOne`, column
      `RESTAURANT_ID`, not null) and `price` (`BigDecimal`, column `PRICE`)
- [x] `Ingredient` (`model/entity/Ingredient.java`) gains `supplier` (`@ManyToOne`, column
      `SUPPLIER_ID`, not null)
- [x] `RestaurantRepository`, `SupplierRepository`, `UserRepository` (`repository/`), each a plain
      `JpaRepository<T, Long>` — no custom finders needed yet
- [x] `ProductRepository` (`repository/ProductRepository.java`) gains
      `List<Product> findByIdInAndRestaurantId(List<Long> productIds, Long restaurantId)`, replacing
      the call site's use of `findByIdIn`

## 2. Liquibase changesets
*Depends on 1.*
- [x] `003-restaurants-suppliers-accounts-schema.yaml`: `createSequence` for
      `restaurant_id_seq`/`supplier_id_seq`/`user_id_seq`; `createTable` for `restaurant`, `supplier`,
      `app_user` (avoid the reserved word `user`); `addColumn` on `product` for `restaurant_id` (BIGINT,
      not null) and `price` (NUMERIC(10,2)); `addColumn` on `ingredient` for `supplier_id` (BIGINT, not
      null); foreign keys for all four new/altered columns; a unique constraint on `app_user.email`
- [x] `004-seed-restaurant-supplier-accounts.yaml`, context `seed-data`: insert one `restaurant`
      (owns both seeded products), one `supplier` (supplies all four seeded ingredients), one `app_user`
      per `Role` with a precomputed BCrypt hash as the seeded password's literal value; `update` the
      four existing `product`/`ingredient` seed rows to set `restaurant_id`/`supplier_id`/`price`
      (`price` needed since Phase 12 makes the column not-null); advance the three new sequences past
      the seeded ids, matching the pattern in `002-seed-catalogue-data.yaml`'s `010-advance-catalogue-sequences`
- [x] Both new files added to `db.changelog-master.yaml`'s `include` list

## 3. Order flow scoping
*Depends on 1, 2.*
- [x] `OrderRequest` (`model/request/OrderRequest.java`) gains `Long restaurantId` with `@NotNull`
- [x] `OrderController.order` (`controller/OrderController.java`) annotates the `@RequestBody` with
      `@Valid` so a missing `restaurantId` produces the existing `MethodArgumentNotValidException` →
      `VALIDATION_FAILED` (`E0002`) path
- [x] `ProductNotFoundException` (`exception/ProductNotFoundException.java`): constructed with the
      `List<Long>` of missing/foreign product ids, message naming them (e.g.
      `"Product(s) not found: 3, 4"`)
- [x] `ErrorCode` (`exception/ErrorCode.java`) gains `PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND)`
      appended after `UNCLASSIFIED_ERROR`, preserving every existing code's number
- [x] `FoodicsChallengeControllerAdvice` gains a handler for `ProductNotFoundException` →
      `404` with `ErrorCode.PRODUCT_NOT_FOUND`, message from the exception
- [x] `ProductService.getAllProductsByIdIn` replaced with `getAllProductsByRestaurant(Long
      restaurantId, List<Long> productIds)`: calls `findByIdInAndRestaurantId`, diffs the returned
      products' ids against `productIds`, throws `ProductNotFoundException` with whatever's missing
- [x] `OrderService.order` (`service/OrderService.java`) passes `orderRequest.restaurantId()` through
      to `ProductService`

## 4. Alert recipient
*Depends on 1, 2.*
- [x] `IngredientService` (`service/IngredientService.java`) drops the `MERCHANT_EMAIL` constant;
      `updateIngredientsStock` groups nothing yet (Phase 15's job) but sends to
      `ingredient.getSupplier().getEmail()` for the (today, single) supplier behind the ingredients
      that crossed the threshold — since the seed data gives every ingredient the same supplier, one
      email still goes out per order, matching today's observable behavior

## 5. Tests
*Depends on 1–4.*
- [x] `OrderControllerTest` (`controller/OrderControllerTest.java`): update existing requests to
      include `restaurantId`; add a case for a missing `restaurantId` (`400`/`E0002`), a case for a
      `productId` that doesn't exist at all (`404`/`ErrorCode.PRODUCT_NOT_FOUND`'s code), and a case
      for a `productId` that exists but belongs to a different restaurant (same `404`) — the latter
      needs a second seeded-in-test restaurant/product not on the main restaurant's menu
- [x] `OrderServiceIntegrationTest` (or wherever the Phase 4/5 integration coverage lives): update
      existing orders to pass `restaurantId`; add coverage that stock depletion and the 50%-threshold
      alert still work unchanged with the scoped resolution in place
- [x] New test (unit or slice) proving `IngredientService` now emails the ingredient's supplier
      address, not a hard-coded constant
- [x] New test proving the Liquibase-seeded data matches the exit criteria: one restaurant owning
      both seeded products, one supplier on all four ingredients, one `app_user` per role — a
      lightweight repository-level assertion is enough, no need for a dedicated Liquibase test harness
