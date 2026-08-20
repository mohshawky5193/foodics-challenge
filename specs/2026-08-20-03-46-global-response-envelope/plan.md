# Phase 10 — Global response envelope — Plan

## 1. Envelope model and error registry
- [x] `src/main/java/com/foodics/challenge/model/response/ApiResponse.java` — record
      `ApiResponse<T>(String code, String status, T data)`
- [x] `src/main/java/com/foodics/challenge/model/response/ApiError.java` — record
      `ApiError(String code, String message)`, the marker exception handlers return
- [x] `src/main/java/com/foodics/challenge/exception/ErrorCode.java` — enum with one entry per handled
      exception (`INSUFFICIENT_INGREDIENTS`, `VALIDATION_FAILED`, `MALFORMED_REQUEST_BODY`,
      `INVALID_PARAMETER`, `RESOURCE_NOT_FOUND`, `METHOD_NOT_ALLOWED`, `INTERNAL_ERROR`,
      `UNCLASSIFIED_ERROR`), each carrying its `HttpStatus` and a zero-padded `E%04d` code derived from
      declaration order

## 2. Global response wrapping
*Depends on 1.*
- [x] `src/main/java/com/foodics/challenge/controlleradvice/ApiResponseBodyAdvice.java` —
      `@ControllerAdvice` implementing `ResponseBodyAdvice<Object>`; `supports` returns `true`;
      `beforeBodyWrite` reads the HTTP status reason phrase off the real `ServletServerHttpResponse`,
      unwraps an `ApiError` body into `{code, status, data: message}`, wraps a ≥400 status that isn't an
      `ApiError` as `UNCLASSIFIED_ERROR`, and otherwise wraps as `{code: "S000", status, data: body}`

## 3. Controller and advice cutover
*Depends on 1, 2.*
- [x] `OrderController.order` returns `Boolean` directly (drop `ResponseEntity`, drop the now-unused
      `HttpStatus`/`ResponseEntity` imports)
- [x] `FoodicsChallengeControllerAdvice` becomes `@RestControllerAdvice`; replace the single
      `InsufficientIngredientsException` handler and add one `@ExceptionHandler` + `@ResponseStatus` pair
      per remaining `ErrorCode` entry (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`,
      `MethodArgumentTypeMismatchException`, `NoResourceFoundException`,
      `HttpRequestMethodNotSupportedException`, `Exception` catch-all), each returning `new ApiError(...)`

## 4. Tests
*Depends on 3.*
- [x] Update `OrderControllerTest.postOrder` to assert `$.code == "S000"`, `$.status == "OK"`,
      `$.data == true` instead of a bare `200`
- [x] Update `OrderControllerTest.postOrderWithException` to assert `$.code == "E0001"`,
      `$.status == "Bad Request"`
- [x] Add a test hitting an unmapped path (`GET /does-not-exist`) asserting `404` and `$.code == "E0005"`
      — confirmed `NoResourceFoundException` is indeed what Spring Boot 4/Framework 7 throws here
- [x] Add a test calling `/order` with `GET` instead of `POST`, asserting `405` and `$.code == "E0006"`
- [x] `OrderServiceIntegrationTest` and `FoodicsCodingChallengeApplicationTests` are unaffected (no
      controller-layer assertions) — confirmed still passing, untouched

## 5. Verify
*Depends on 1-4.*
- [x] `mvn clean verify` passes
- [x] `grep -rn ResponseEntity src/main` returns nothing
- [x] Manual: booted the app (H2 defaults + required credentials) and curled `/order` for a success case,
      an insufficient-stock case, an unmapped route, and a wrong-method request — all four matched the
      spec exactly:
      `{"code":"S000","status":"OK","data":true}`,
      `{"code":"E0001","status":"Bad Request","data":"Insufficient ingredients for your order"}`,
      `{"code":"E0005","status":"Not Found","data":"Resource not found"}`,
      `{"code":"E0006","status":"Method Not Allowed","data":"HTTP method not supported"}`
