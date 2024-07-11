package com.foodics.challenge.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.validation.constraints.Min;

@Entity
public class ProductIngredient {
@EmbeddedId
private ProductIngredientKey productIngredientKey;

  @ManyToOne
  @MapsId("productId")
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @ManyToOne
  @MapsId("ingredientId")
  @JoinColumn(name = "INGREDIENT_ID")
  private Ingredient ingredient;
  @Column(name="AMOUNT_IN_GRAMS")
  private Integer amountInGrams;

  public ProductIngredientKey getProductIngredientKey() {
    return productIngredientKey;
  }

  public void setProductIngredientKey(
      ProductIngredientKey productIngredientKey) {
    this.productIngredientKey = productIngredientKey;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Ingredient getIngredient() {
    return ingredient;
  }

  public void setIngredient(Ingredient ingredient) {
    this.ingredient = ingredient;
  }

  public Integer getAmountInGrams() {
    return amountInGrams;
  }

  public void setAmountInGrams(Integer amountInGrams) {
    this.amountInGrams = amountInGrams;
  }
}
