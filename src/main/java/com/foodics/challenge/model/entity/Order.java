package com.foodics.challenge.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ORDER_DATA")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderGen")
  @SequenceGenerator(name = "orderGen", sequenceName = "order_id_seq")
  private Long id;

  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "order",cascade = CascadeType.PERSIST)
  private List<OrderDetail> orderDetails;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public List<OrderDetail> getOrderDetails() {
    return orderDetails;
  }

  public void setOrderDetails(List<OrderDetail> orderDetails) {
    this.orderDetails = orderDetails;
  }


}
