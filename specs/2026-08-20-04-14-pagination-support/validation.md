# Phase 11 — Paginated response support — Validation

## Automated

- `mvn clean verify` — must pass. Covers:
  - New `ApiResponseBodyAdviceTest` — `PagedResult` body → `data` is the item list,
    `paginationInfo` populated; plain body → `paginationInfo` is `null` on the record and absent from
    the serialized JSON.
  - `OrderControllerTest` — unchanged assertions still pass, proving the new `ApiResponse` component is
    additive, not breaking.

## Manual

- None — there is no paginated endpoint to curl yet (out of scope per requirements.md); the automated
  JSON-shape assertions in group 3 are the direct proof of the serialized output.

## Merge criteria

- [x] `mvn test` passes
- [x] A `PagedResult` returned to `ApiResponseBodyAdvice` serializes as
      `{"code":"S000","status":"...","data":[...],"paginationInfo":{...}}`
- [x] Every other response (success or error) still serializes as the three-field
      `{"code":...,"status":...,"data":...}` with no `paginationInfo` key present

## Rollback

`git revert` the phase 11 commit(s) — removes `PaginationInfo`/`PagedResult`, the `ApiResponse` field, and
the advice branch; no endpoint or persisted data depends on this phase, so revert is clean.
