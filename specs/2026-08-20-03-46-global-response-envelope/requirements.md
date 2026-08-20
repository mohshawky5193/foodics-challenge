# Phase 10 — Global response envelope

## Context

Today only two classes touch HTTP response construction: `OrderController.order` returns
`ResponseEntity<Boolean>`, and `FoodicsChallengeControllerAdvice` (plain `@ControllerAdvice`) catches
`InsufficientIngredientsException` and returns `ResponseEntity<String>` with the plain-text message
"Insufficient ingredients for your order". Every response shape is bespoke to its endpoint/handler, and
nothing distinguishes success from failure except the HTTP status code.

This phase wraps every response — success or error — in one consistent JSON envelope, built centrally by
a `ResponseBodyAdvice`, so controllers and exception handlers stop constructing `ResponseEntity` and just
return their data or error payload directly.

## Scope

- `OrderController.order` returns `Boolean` directly; no `ResponseEntity` remains anywhere under
  `controller` or `controlleradvice`.
- `FoodicsChallengeControllerAdvice` becomes `@RestControllerAdvice`; each `@ExceptionHandler` uses
  `@ResponseStatus` for the HTTP status and returns an `ApiError` payload directly.
- A handler (and a numbered error code) for every exception this API can currently raise going through
  Spring MVC's normal dispatch: `InsufficientIngredientsException`, bean-validation failures
  (`MethodArgumentNotValidException`), a malformed request body (`HttpMessageNotReadableException`), a
  type-mismatched parameter (`MethodArgumentTypeMismatchException`), an unmapped route
  (`NoResourceFoundException`), an unsupported HTTP method (`HttpRequestMethodNotSupportedException`),
  and anything else (`Exception`, catch-all) — numbered `E0001`…`E0007` in that order.
- A global `ApiResponseBodyAdvice implements ResponseBodyAdvice<Object>` wraps every response body:
  - success → `{"code":"S000","status":"<HTTP reason phrase>","data":<original return value>}`
  - a handled error → `{"code":"E000N","status":"<HTTP reason phrase>","data":"<handler's message>"}`
  - anything else that reaches the advice with a 4xx/5xx status without going through one of the above
    handlers (e.g. a container-level error reaching Boot's `BasicErrorController`) → a fallback
    `E0008` "unclassified" code, so nothing with an error status is ever mislabeled `S000`
- Existing `OrderControllerTest` assertions updated for the new envelope shape (`$.code`, `$.status`,
  `$.data` JSON paths instead of a bare boolean/string body); new tests cover the success path and each
  of `E0001` (insufficient ingredients), `E0005` (unmapped route), `E0006` (wrong HTTP method).

## Out of scope

- Adding `@Valid` to `OrderRequest`/`ProductRequest` or any bean-validation constraints. The
  `MethodArgumentNotValidException` handler is added for completeness (and because the roadmap's Phase 13
  already earmarks "`@Valid` on every payload" as RBAC-phase work), but nothing in this phase triggers it
  yet — that's expected and fine, not a bug to chase down.
- `MethodArgumentTypeMismatchException` is likewise unreachable today (no path/query parameters exist
  yet); it's added because Phase 15's `GET /restaurants/{restaurantId}/menu` will have one.
- Changing what `InsufficientIngredientsException`'s message says, or any ordering/alerting behaviour.
- Restaurants, suppliers, accounts, auth — that's Phase 11 onward, unaffected by this phase beyond
  inheriting the envelope once those endpoints exist.

## Decisions

- **`ApiError` as an internal marker record, not a request attribute.** Each exception handler returns
  `new ApiError(code, message)` directly (satisfying "return the object direct"). `ApiResponseBodyAdvice`
  recognizes `body instanceof ApiError` and unwraps it into the envelope, reading the numbered code off
  the marker itself. The alternative — stashing the resolved code in a request attribute and reading it
  back via `HttpServletRequest` in the advice — works too, but needs casting `ServerHttpRequest` to
  `ServletServerHttpRequest` for no real benefit; the marker keeps everything in normal Java objects.
- **HTTP status reason phrase read from the actual response, not recomputed.** `beforeBodyWrite` casts
  `ServerHttpResponse` to `ServletServerHttpResponse` and reads `getServletResponse().getStatus()` — the
  status Spring already set from `@ResponseStatus` (errors) or the controller method's default (200,
  success) — rather than trying to re-derive it from the exception type. One source of truth.
- **Error codes are zero-padded to four digits (`E0001`), per the roadmap's explicit "numbered from
  0001"**, even though the roadmap's own illustrative example (`E123`) is three digits — the numbered-list
  instruction is the binding one.
- **A numbered `E0008` fallback for anything not routed through our own handlers.** Without it, a
  container-level error that reaches `BasicErrorController` (bypassing our `@ExceptionHandler`s entirely)
  would still flow through `ApiResponseBodyAdvice` and — since it's not an `ApiError` — get mislabeled
  `S000` despite carrying a 4xx/5xx status. Any body reaching the advice with status ≥ 400 that isn't
  already an `ApiError` gets this fallback code instead.
- **`ErrorCode` enum owns the number↔status pairing**, so the numbering is declared in one place
  (declaration order = numbering order) instead of being repeated as magic strings across handler
  methods.
- **`NoResourceFoundException`/`HttpRequestMethodNotSupportedException` class names are Spring
  Framework's current (7.x/Boot 4) ones** — confirmed empirically during implementation (group 4's tests
  hit an unmapped route and a wrong-method request directly), not assumed from memory, since MVC's
  no-handler-found behavior has changed across recent framework versions.

## Open questions

None — verified empirically during implementation where the design notes it (see Decisions, last bullet).
