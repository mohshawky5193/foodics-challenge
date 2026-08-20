# Phase 9 — Liquibase-managed schema — Plan

## 1. Add Liquibase and the baseline schema changelog
- [x] Add the `org.springframework.boot:spring-boot-starter-liquibase` dependency to `pom.xml` (no
      explicit version; plain `liquibase-core` resolves but its autoconfiguration never fires on Boot 4
      — see requirements.md)
- [x] Drop `<scope>test</scope>` from the `com.h2database:h2` dependency so the H2 default from Phase 8
      can actually boot outside a test JVM
- [x] Create `src/main/resources/db/changelog/db.changelog-master.yaml` including
      `changes/001-baseline-schema.yaml` and (for now, as a placeholder to fill in group 2)
      `changes/002-seed-catalogue-data.yaml`
- [x] Create `src/main/resources/db/changelog/changes/001-baseline-schema.yaml` with changeSets for:
      `product_id_seq`/`ingredient_id_seq`/`order_id_seq` (`startValue: 1, incrementBy: 50`); the
      `product`, `ingredient`, `order_data` tables; the `product_ingredient` and `order_detail` tables
      with their composite primary keys and foreign keys to `product`/`ingredient`/`order_data`
- [x] Add `spring.liquibase.change-log: classpath:db/changelog/db.changelog-master.yaml` to
      `application.yaml` (matches the Spring Boot default; set explicitly for discoverability)
- [x] Change `spring.jpa.hibernate.ddl-auto` from `create-drop` to `validate` in `application.yaml`
- [x] Create an empty (no changeSets yet) `src/main/resources/db/changelog/changes/002-seed-catalogue-data.yaml`
      so the master changelog's `include` resolves — filled in by group 2

## 2. Move seed data into a changeset
*Depends on 1.*
- [ ] Fill in `changes/002-seed-catalogue-data.yaml`: `insert` changeSets (context `seed-data`) for the
      two `product` rows (`id: 1` Burger, `id: 2` Chicken Burger), four `ingredient` rows (`id: 1` Beef
      20000g, `id: 2` Chicken 20000g, `id: 3` Cheese 5000g, `id: 4` Onion 1000g — `consumed_amount_in_grams`
      omitted so it inserts as `NULL`), and six `product_ingredient` rows (burger→beef 150g, burger→cheese
      30g, burger→onion 20g, chicken burger→chicken 150g, chicken burger→cheese 30g, chicken
      burger→onion 20g)
- [ ] Add a `sql` changeSet (context `seed-data`) restarting `product_id_seq` and `ingredient_id_seq` at
      51, after the inserts
- [ ] Delete `src/main/java/com/foodics/challenge/config/DatabaseInitializer.java`
- [ ] Remove `DatabaseInitializer.class` from `OrderServiceIntegrationTest`'s `@Import` and delete the
      now-unused `import com.foodics.challenge.config.DatabaseInitializer;`

## 3. Verify
*Depends on 1, 2.*
- [ ] `mvn clean verify` passes — Hibernate's `validate` accepts the Liquibase-built H2 schema, and the
      existing `OrderServiceIntegrationTest`/`OrderControllerTest`/`FoodicsCodingChallengeApplicationTests`
      assertions (including the exact-3-non-null-ingredients check) still hold against Liquibase-seeded
      data
- [ ] Confirm no `DatabaseInitializer` reference remains (`grep -rn DatabaseInitializer src`)
