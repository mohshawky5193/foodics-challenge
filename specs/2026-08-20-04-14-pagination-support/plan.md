# Phase 11 — Paginated response support — Plan

## 1. Pagination model
- [x] `src/main/java/com/foodics/challenge/model/response/PaginationInfo.java` — record
      `PaginationInfo(int page, int size, long totalElements, int totalPages)` with a static
      `from(org.springframework.data.domain.Page<?> page)` factory
- [x] `src/main/java/com/foodics/challenge/model/response/PagedResult.java` — record
      `PagedResult<T>(List<T> items, PaginationInfo paginationInfo)`

## 2. Envelope and advice update
*Depends on 1.*
- [x] `ApiResponse` gains a fourth component `PaginationInfo paginationInfo`; class annotated
      `@JsonInclude(JsonInclude.Include.NON_NULL)` (`com.fasterxml.jackson.annotation.JsonInclude`) so the
      field is omitted, not nulled, when absent
- [x] Update the three existing `new ApiResponse<>(...)` call sites in `ApiResponseBodyAdvice` to pass
      `null` for `paginationInfo`
- [x] Add a `body instanceof PagedResult<?> paged` branch in `beforeBodyWrite`, checked before the
      generic success/error fallthrough, returning
      `new ApiResponse<>(SUCCESS_CODE, status, paged.items(), paged.paginationInfo())`

## 3. Tests
*Depends on 2.*
- [x] New test class `ApiResponseBodyAdviceTest` (unit test, no Spring context): construct
      `MockHttpServletResponse`/`ServletServerHttpResponse`, call `beforeBodyWrite` directly with a
      `PagedResult` body and assert the returned `ApiResponse`'s `data`/`paginationInfo`
- [x] Same test class: call `beforeBodyWrite` with a plain body (`Boolean`) on a 200 response and assert
      `paginationInfo()` is `null` on the returned `ApiResponse`
- [x] Serialize both cases with a real Jackson 3 mapper (`JsonMapper.builder().build()` — confirmed this
      is the correct construction pattern for `tools.jackson.databind.json.JsonMapper` by running it, not
      assuming) and assert the paginated case's JSON contains `"paginationInfo"` while the non-paginated
      case's JSON does not contain that key at all
- [x] Confirmed existing `OrderControllerTest` assertions (`$.code`, `$.status`, `$.data`) are unaffected
      by the new `ApiResponse` component — reran unmodified, still green

## 4. Verify
*Depends on 1-3.*
- [x] `mvn clean verify` passes
- [x] Manual: skipped — no endpoint exists to curl; group 3's direct JSON-shape assertions are the proof
