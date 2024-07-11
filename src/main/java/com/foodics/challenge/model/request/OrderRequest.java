package com.foodics.challenge.model.request;

import java.util.List;

public class OrderRequest {

  private List<ProductRequest> products;

  public List<ProductRequest> getProducts() {
    return products;
  }

  public void setProducts(List<ProductRequest> products) {
    this.products = products;
  }
}
