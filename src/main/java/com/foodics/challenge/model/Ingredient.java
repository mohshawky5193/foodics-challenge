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
@Table(name ="INGREDIENT")
public class Ingredient {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ingredientGen")
  @SequenceGenerator(name = "ingredientGen", sequenceName = "ingredient_id_seq")
  private Long Id;

  @Column(name="NAME")
  private String name;

  @Column(name="AMOUNT_IN_GRAMS")
  private Integer amountInGrams;

  @OneToMany(mappedBy = "ingredient")
  private List<ProductIngredient> productIngredients;

  public Long getId() {
    return Id;
  }

  public void setId(Long id) {
    Id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAmountInGrams() {
    return amountInGrams;
  }

  public void setAmountInGrams(Integer amountInGrams) {
    this.amountInGrams = amountInGrams;
  }

  public List<ProductIngredient> getProductIngredients() {
    return productIngredients;
  }

  public void setProductIngredients(
      List<ProductIngredient> productIngredients) {
    this.productIngredients = productIngredients;
  }
}
