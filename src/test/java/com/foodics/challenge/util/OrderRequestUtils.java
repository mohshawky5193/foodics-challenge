package com.foodics.challenge.util;

import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.model.request.ProductRequest;
import java.util.List;

public class OrderRequestUtils {

  public static OrderRequest noEmailOrderRequest() {
    return new OrderRequest(List.of(new ProductRequest(1L, 2), new ProductRequest(2L, 2)));
  }

  public static OrderRequest orderRequestCausingEmail() {
    return new OrderRequest(List.of(new ProductRequest(1L, 26)));
  }

  public static OrderRequest orderRequestInsufficientIngredients(){
    return new OrderRequest(List.of(new ProductRequest(1L, 100)));
  }
}
