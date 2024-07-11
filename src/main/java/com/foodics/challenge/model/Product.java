package com.foodics.challenge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "PRODUCT")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productGen")
  @SequenceGenerator(name = "productGen", sequenceName = "product_id_seq")
  private Long id;

  @Column(name="NAME")
  private String name;

  @OneToMany(mappedBy = "product")
  private List<ProductIngredient> productIngredients;

  @OneToMany(mappedBy = "product")
  private List<OrderDetails> orderDetails;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ProductIngredient> getProductIngredients() {
    return productIngredients;
  }

  public void setProductIngredients(
      List<ProductIngredient> productIngredients) {
    this.productIngredients = productIngredients;
  }

  public List<OrderDetails> getOrderDetails() {
    return orderDetails;
  }

  public void setOrderDetails(List<OrderDetails> orderDetails) {
    this.orderDetails = orderDetails;
  }
}
