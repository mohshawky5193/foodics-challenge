package com.foodics.challenge.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class OrderDetailsKey {

  @Column(name = "PRODUCT_ID")
  private Long productId;

  @Column(name = "ORDER_ID")
  private Long orderId;


  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderDetailsKey that = (OrderDetailsKey) o;
    return Objects.equals(productId, that.productId) && Objects.equals(orderId,
        that.orderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, orderId);
  }
}
