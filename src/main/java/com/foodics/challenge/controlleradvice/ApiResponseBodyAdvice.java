package com.foodics.challenge.controlleradvice;

import com.foodics.challenge.exception.ErrorCode;
import com.foodics.challenge.model.response.ApiError;
import com.foodics.challenge.model.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  private static final String SUCCESS_CODE = "S000";

  @Override
  public boolean supports(MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType,
      MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request, ServerHttpResponse response) {
    int statusCode = resolveStatusCode(response);
    String status = HttpStatus.valueOf(statusCode).getReasonPhrase();

    if (body instanceof ApiError apiError) {
      return new ApiResponse<>(apiError.code(), status, apiError.message());
    }
    if (statusCode >= 400) {
      return new ApiResponse<>(ErrorCode.UNCLASSIFIED_ERROR.code(), status, body);
    }
    return new ApiResponse<>(SUCCESS_CODE, status, body);
  }

  private int resolveStatusCode(ServerHttpResponse response) {
    if (response instanceof ServletServerHttpResponse servletResponse) {
      return servletResponse.getServletResponse().getStatus();
    }
    return HttpStatus.OK.value();
  }
}
