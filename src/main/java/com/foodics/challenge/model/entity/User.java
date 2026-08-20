package com.foodics.challenge.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "APP_USER")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userGen")
  @SequenceGenerator(name = "userGen", sequenceName = "user_id_seq")
  private Long id;

  @Column(name = "EMAIL")
  private String email;

  @Column(name = "PASSWORD_HASH")
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "ROLE")
  private Role role;

  @ManyToOne
  @JoinColumn(name = "RESTAURANT_ID")
  private Restaurant restaurant;

  @ManyToOne
  @JoinColumn(name = "SUPPLIER_ID")
  private Supplier supplier;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Restaurant getRestaurant() {
    return restaurant;
  }

  public void setRestaurant(Restaurant restaurant) {
    this.restaurant = restaurant;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }
}
