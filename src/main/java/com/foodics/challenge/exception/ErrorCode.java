package com.foodics.challenge.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  INSUFFICIENT_INGREDIENTS(HttpStatus.BAD_REQUEST),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
  MALFORMED_REQUEST_BODY(HttpStatus.BAD_REQUEST),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
  UNCLASSIFIED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND);

  private final String code;
  private final HttpStatus httpStatus;

  ErrorCode(HttpStatus httpStatus) {
    this.code = "E%04d".formatted(ordinal() + 1);
    this.httpStatus = httpStatus;
  }

  public String code() {
    return code;
  }

  public HttpStatus httpStatus() {
    return httpStatus;
  }
}
