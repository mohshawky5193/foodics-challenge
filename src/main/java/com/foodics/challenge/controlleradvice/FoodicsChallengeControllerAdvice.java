package com.foodics.challenge.controlleradvice;

import com.foodics.challenge.exception.ErrorCode;
import com.foodics.challenge.exception.InsufficientIngredientsException;
import com.foodics.challenge.model.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class FoodicsChallengeControllerAdvice {

  private static final String INSUFFICIENT_INGREDIENTS_MESSAGE = "Insufficient ingredients for your order";
  private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";
  private static final String MALFORMED_REQUEST_BODY_MESSAGE = "Malformed request body";
  private static final String INVALID_PARAMETER_MESSAGE = "Invalid request parameter";
  private static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource not found";
  private static final String METHOD_NOT_ALLOWED_MESSAGE = "HTTP method not supported";
  private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred";

  @ExceptionHandler(InsufficientIngredientsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleInsufficientIngredients(InsufficientIngredientsException exception) {
    return new ApiError(ErrorCode.INSUFFICIENT_INGREDIENTS.code(), INSUFFICIENT_INGREDIENTS_MESSAGE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleValidation(MethodArgumentNotValidException exception) {
    return new ApiError(ErrorCode.VALIDATION_FAILED.code(), VALIDATION_FAILED_MESSAGE);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleMalformedRequestBody(HttpMessageNotReadableException exception) {
    return new ApiError(ErrorCode.MALFORMED_REQUEST_BODY.code(), MALFORMED_REQUEST_BODY_MESSAGE);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
    return new ApiError(ErrorCode.INVALID_PARAMETER.code(), INVALID_PARAMETER_MESSAGE);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handleResourceNotFound(NoResourceFoundException exception) {
    return new ApiError(ErrorCode.RESOURCE_NOT_FOUND.code(), RESOURCE_NOT_FOUND_MESSAGE);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public ApiError handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
    return new ApiError(ErrorCode.METHOD_NOT_ALLOWED.code(), METHOD_NOT_ALLOWED_MESSAGE);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleUnexpected(Exception exception) {
    return new ApiError(ErrorCode.INTERNAL_ERROR.code(), INTERNAL_ERROR_MESSAGE);
  }
}
