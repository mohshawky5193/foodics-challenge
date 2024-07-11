package com.foodics.challenge.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

@Entity
public class OrderDetail {

  @EmbeddedId
  private OrderDetailsKey orderDetailsKey;

  @ManyToOne
  @MapsId("productId")
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @ManyToOne
  @MapsId("orderId")
  @JoinColumn(name = "ORDER_ID")
  private Order order;

  @Column(name= "QUANTITY")
  private Integer quantity;

  public OrderDetailsKey getOrderDetailsKey() {
    return orderDetailsKey;
  }

  public void setOrderDetailsKey(OrderDetailsKey orderDetailsKey) {
    this.orderDetailsKey = orderDetailsKey;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}
