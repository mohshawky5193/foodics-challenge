package com.foodics.challenge.controller;

import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/order")
  public Boolean order(@Valid @RequestBody OrderRequest orderRequest){
    orderService.order(orderRequest);
    return true;
  }
}
