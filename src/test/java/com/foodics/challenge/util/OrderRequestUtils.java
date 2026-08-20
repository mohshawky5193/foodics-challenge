package com.foodics.challenge.util;

import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.model.request.ProductRequest;
import java.util.List;

public class OrderRequestUtils {

  public static final Long SEEDED_RESTAURANT_ID = 1L;

  public static OrderRequest noEmailOrderRequest() {
    return new OrderRequest(SEEDED_RESTAURANT_ID,
        List.of(new ProductRequest(1L, 2), new ProductRequest(2L, 2)));
  }

  public static OrderRequest orderRequestCausingEmail() {
    return new OrderRequest(SEEDED_RESTAURANT_ID, List.of(new ProductRequest(1L, 26)));
  }

  public static OrderRequest orderRequestInsufficientIngredients(){
    return new OrderRequest(SEEDED_RESTAURANT_ID, List.of(new ProductRequest(1L, 100)));
  }

  public static OrderRequest orderRequestUnknownProduct() {
    return new OrderRequest(SEEDED_RESTAURANT_ID, List.of(new ProductRequest(999L, 1)));
  }

  public static OrderRequest orderRequestUnknownRestaurant() {
    return new OrderRequest(999L, List.of(new ProductRequest(1L, 1)));
  }

  public static OrderRequest orderRequestMissingRestaurantId() {
    return new OrderRequest(null, List.of(new ProductRequest(1L, 1)));
  }
}
