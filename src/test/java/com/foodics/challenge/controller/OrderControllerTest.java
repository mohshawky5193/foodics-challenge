package com.foodics.challenge.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.foodics.challenge.exception.InsufficientIngredientsException;
import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.service.OrderService;
import com.foodics.challenge.util.OrderRequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest
@AutoConfigureMockMvc
public class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrderService orderService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void postOrder() throws Exception {

    OrderRequest orderRequest= OrderRequestUtils.noEmailOrderRequest();
    mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S000"))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.data").value(true));
  }

  @Test
  void postOrderWithException() throws Exception {
    OrderRequest orderRequest = OrderRequestUtils.orderRequestInsufficientIngredients();
    doThrow(new InsufficientIngredientsException()).when(orderService).order(any(OrderRequest.class));

    mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("E0001"))
        .andExpect(jsonPath("$.status").value("Bad Request"))
        .andExpect(jsonPath("$.data").value("Insufficient ingredients for your order"));
  }

  @Test
  void getUnmappedRouteReturnsNotFound() throws Exception {
    mockMvc.perform(get("/does-not-exist"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("E0005"))
        .andExpect(jsonPath("$.status").value("Not Found"));
  }

  @Test
  void getOrderReturnsMethodNotAllowed() throws Exception {
    mockMvc.perform(get("/order"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.code").value("E0006"))
        .andExpect(jsonPath("$.status").value("Method Not Allowed"));
  }
}
