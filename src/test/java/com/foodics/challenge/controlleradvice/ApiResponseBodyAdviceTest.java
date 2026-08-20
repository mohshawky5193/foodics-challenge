package com.foodics.challenge.controlleradvice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodics.challenge.model.response.ApiResponse;
import com.foodics.challenge.model.response.PaginationInfo;
import com.foodics.challenge.model.response.PagedResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class ApiResponseBodyAdviceTest {

  private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();
  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void wrapsPagedResultWithSiblingPaginationInfo() {
    PaginationInfo paginationInfo = new PaginationInfo(0, 20, 2L, 1);
    PagedResult<String> pagedResult = new PagedResult<>(List.of("Burger", "Chicken Burger"), paginationInfo);
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    mockResponse.setStatus(200);

    Object result = advice.beforeBodyWrite(pagedResult, null, null, null, null,
        new ServletServerHttpResponse(mockResponse));

    ApiResponse<?> apiResponse = (ApiResponse<?>) result;
    assertEquals("S000", apiResponse.code());
    assertEquals(List.of("Burger", "Chicken Burger"), apiResponse.data());
    assertEquals(paginationInfo, apiResponse.paginationInfo());
  }

  @Test
  void leavesPaginationInfoNullForNonPagedBody() {
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    mockResponse.setStatus(200);

    Object result = advice.beforeBodyWrite(true, null, null, null, null,
        new ServletServerHttpResponse(mockResponse));

    ApiResponse<?> apiResponse = (ApiResponse<?>) result;
    assertEquals("S000", apiResponse.code());
    assertEquals(true, apiResponse.data());
    assertNull(apiResponse.paginationInfo());
  }

  @Test
  void paginationInfoKeyIsOmittedWhenAbsentButPresentWhenPopulated() {
    ApiResponse<Boolean> withoutPagination = new ApiResponse<>("S000", "OK", true, null);
    ApiResponse<List<String>> withPagination = new ApiResponse<>("S000", "OK", List.of("Burger"),
        new PaginationInfo(0, 20, 1L, 1));

    String withoutJson = jsonMapper.writeValueAsString(withoutPagination);
    String withJson = jsonMapper.writeValueAsString(withPagination);

    assertFalse(withoutJson.contains("paginationInfo"), "expected no paginationInfo key: " + withoutJson);
    assertTrue(withJson.contains("paginationInfo"), "expected paginationInfo key: " + withJson);
  }
}
