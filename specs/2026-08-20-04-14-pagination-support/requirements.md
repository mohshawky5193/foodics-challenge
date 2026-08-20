# Phase 11 — Paginated response support

## Context

Phase 10 gave every response a `{code, status, data}` envelope via `ApiResponseBodyAdvice`
(`src/main/java/com/foodics/challenge/controlleradvice/ApiResponseBodyAdvice.java`), recognizing an
`ApiError` marker to distinguish an error body from a success body. There is no list endpoint anywhere in
the app yet — `Product`/`Ingredient` are seed-only per the mission's explicit "no CRUD endpoints" scope,
and the only endpoint is `POST /order`. But a future list endpoint (the roadmap's own Phase 16 menu
endpoint, or anything added later) will need pagination metadata alongside the returned items, and
retrofitting the envelope shape after real callers depend on it is worse than building the extension
point now, while it's cheap and isolated.

This phase adds that extension point: a `PagedResult` marker (parallel to `ApiError`) that
`ApiResponseBodyAdvice` recognizes and unwraps into `data` (the item list) plus a sibling
`paginationInfo` field — without changing the shape of any existing non-paginated response.

## Scope

- `PaginationInfo` record (`page`, `size`, `totalElements`, `totalPages`) with a
  `from(org.springframework.data.domain.Page<?>)` factory, so any future controller backed by a Spring
  Data `Page<T>` can build one in one line.
- `PagedResult<T>` record (`items`, `paginationInfo`) — the marker a controller would return for a
  paginated list.
- `ApiResponse` gains a fourth component, `paginationInfo`, defaulting to `null` and annotated so Jackson
  omits it entirely (not `"paginationInfo":null`) when absent.
- `ApiResponseBodyAdvice.beforeBodyWrite` gains a branch: `body instanceof PagedResult<?> paged` →
  `data = paged.items()`, `paginationInfo = paged.paginationInfo()`.
- Tests proving both shapes: a `PagedResult` body produces `data` as the list and a populated
  `paginationInfo`; every existing non-paginated case (success, each error) still serializes with no
  `paginationInfo` key at all.

## Out of scope

- Any actual list/paginated endpoint. The mission (`specs/mission.md`) is explicit that product/ingredient
  CRUD endpoints — the natural candidates for pagination — are out of scope; the catalogue stays seeded.
  This phase is pure envelope infrastructure, validated directly against `ApiResponseBodyAdvice` and
  `ApiResponse` rather than through a controller round-trip, since there is nothing to route to yet.
- Query-parameter parsing, sorting, or a `Pageable`-binding controller convention — those belong to
  whichever future phase adds the first paginated endpoint (Phase 16 is the current candidate) and can
  reuse `PaginationInfo`/`PagedResult` without this phase having to guess that endpoint's shape.
- Changing `ErrorCode`/`ApiError` or any existing exception handler.

## Decisions

- **`PagedResult` is a marker record recognized by `instanceof`, exactly like `ApiError`.** Keeps the
  advice's dispatch logic in one place and one style — no request attributes, no second interface to
  implement.
- **`@JsonInclude(JsonInclude.Include.NON_NULL)` on `ApiResponse`, using
  `com.fasterxml.jackson.annotation.JsonInclude`.** Boot 4's Jackson stack here is Jackson 3
  (`tools.jackson.core:jackson-databind:3.1.4`, confirmed via `mvn dependency:tree`), but the annotations
  module it depends on is still the classic `com.fasterxml.jackson.core:jackson-annotations:2.21` —
  `@JsonInclude` lives at its usual package, not under `tools.jackson.*`. Applied at the class level so
  every field defaulting to `null` (currently only `paginationInfo`) is omitted uniformly, rather than
  annotating the field alone.
- **`PaginationInfo.from(Page<?>)` takes Spring Data's `Page` directly** rather than four raw ints, so a
  future controller backed by `JpaRepository.findAll(Pageable)` builds a `PagedResult` in one line
  (`new PagedResult<>(page.getContent(), PaginationInfo.from(page))`) instead of manually unpacking
  `getNumber()`/`getSize()`/etc. every time.
- **Validated at the advice/model level, not through a real endpoint.** Building a throwaway controller
  just to exercise this would either violate the mission's no-CRUD-endpoint scope or need deleting right
  after — a direct unit test on `ApiResponseBodyAdvice.beforeBodyWrite` (constructing a real
  `MockHttpServletResponse` wrapped in `ServletServerHttpResponse`, matching how the advice actually reads
  status) proves the same wiring without a fake production endpoint.

## Open questions

None.
