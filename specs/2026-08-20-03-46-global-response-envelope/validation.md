# Phase 10 — Global response envelope — Validation

## Automated

- `mvn clean verify` — must pass. Covers:
  - `OrderControllerTest.postOrder` — success envelope (`S000`/`OK`/`data: true`)
  - `OrderControllerTest.postOrderWithException` — `E0001`/`Bad Request` for
    `InsufficientIngredientsException`
  - New unmapped-route test — `404`, `E0005`
  - New wrong-method test — `405`, `E0006`
  - `OrderServiceIntegrationTest`, `FoodicsCodingChallengeApplicationTests` — unchanged, still green,
    proving the envelope change is purely a controller/advice-layer concern
- `grep -rn ResponseEntity src/main` — must return no matches.

## Manual

- Run the app with the required env vars (`DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`,
  `MAIL_PASSWORD`; H2 defaults cover the rest) and `POST /order` with a fulfillable order — response body
  is `{"code":"S000","status":"OK","data":true}`.
- `POST /order` with an order that exceeds stock — response body is
  `{"code":"E0001","status":"Bad Request","data":"Insufficient ingredients for your order"}` with HTTP
  `400`.

## Merge criteria

- [x] `POST /order` returns `{"code":"S000","status":"OK","data":true}` on success
- [x] Each mapped error returns its own `E000N` code with the matching HTTP status
- [x] `mvn test` passes
- [x] No `ResponseEntity` remains under `src/main`

## Rollback

`git revert` the phase 10 commit(s) — reintroduces `ResponseEntity` in `OrderController` and
`FoodicsChallengeControllerAdvice` and removes the envelope classes; no persistent state is touched by
this phase, so revert is clean.
