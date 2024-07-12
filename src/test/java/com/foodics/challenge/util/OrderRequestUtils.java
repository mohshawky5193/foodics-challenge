package com.foodics.challenge.util;

import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.model.request.ProductRequest;
import java.util.List;

public class OrderRequestUtils {

  public static OrderRequest noEmailOrderRequest() {
    OrderRequest orderRequest = new OrderRequest();
    ProductRequest productRequest = new ProductRequest();
    productRequest.setProductId(1L);
    productRequest.setQuantity(2);

    ProductRequest productRequest2 = new ProductRequest();
    productRequest2.setProductId(2L);
    productRequest2.setQuantity(2);
    orderRequest.setProducts(List.of(productRequest,productRequest2));
    return orderRequest;
  }

  public static OrderRequest orderRequestCausingEmail() {
    OrderRequest orderRequest = new OrderRequest();
    ProductRequest productRequest = new ProductRequest();
    productRequest.setProductId(1L);
    productRequest.setQuantity(26);
    orderRequest.setProducts(List.of(productRequest));
    return orderRequest;
  }

  public static OrderRequest orderRequestInsufficientIngredients(){
    OrderRequest orderRequest = new OrderRequest();
    ProductRequest productRequest = new ProductRequest();
    productRequest.setProductId(1L);
    productRequest.setQuantity(100);
    orderRequest.setProducts(List.of(productRequest));
    return orderRequest;
  }
}
