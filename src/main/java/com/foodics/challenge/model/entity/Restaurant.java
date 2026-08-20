package com.foodics.challenge.model.entity;

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
@Table(name = "RESTAURANT")
public class Restaurant {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restaurantGen")
  @SequenceGenerator(name = "restaurantGen", sequenceName = "restaurant_id_seq")
  private Long id;

  @Column(name = "NAME")
  private String name;

  @OneToMany(mappedBy = "restaurant")
  private List<Product> products;

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

  public List<Product> getProducts() {
    return products;
  }

  public void setProducts(List<Product> products) {
    this.products = products;
  }
}
