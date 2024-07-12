package com.foodics.challenge.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodics.challenge.exception.InsufficientIngredientsException;
import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.service.OrderService;
import com.foodics.challenge.util.OrderRequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@AutoConfigureMockMvc
public class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OrderService orderService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void postOrder() throws Exception {

    OrderRequest orderRequest= OrderRequestUtils.noEmailOrderRequest();
    mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderRequest))).andExpect(status().isOk());
  }

  @Test
  void postOrderWithException() throws Exception {
    OrderRequest orderRequest = OrderRequestUtils.orderRequestInsufficientIngredients();
    doThrow(new InsufficientIngredientsException()).when(orderService).order(any(OrderRequest.class));

    mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderRequest))).andExpect(status().isBadRequest());
  }
}
