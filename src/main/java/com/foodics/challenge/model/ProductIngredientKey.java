package com.foodics.challenge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ProductIngredientKey {

  @Column(name = "PRODUCT_ID")
  private Long productId;

  @Column(name = "INGREDIENT_ID")
  private Long ingredientId;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getIngredientId() {
    return ingredientId;
  }

  public void setIngredientId(Long ingredientId) {
    this.ingredientId = ingredientId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductIngredientKey that = (ProductIngredientKey) o;
    return Objects.equals(productId, that.productId) && Objects.equals(
        ingredientId, that.ingredientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, ingredientId);
  }
}
