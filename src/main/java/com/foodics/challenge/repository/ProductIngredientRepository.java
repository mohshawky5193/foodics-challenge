package com.foodics.challenge.repository;

import com.foodics.challenge.model.entity.ProductIngredient;
import com.foodics.challenge.model.entity.ProductIngredientKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, ProductIngredientKey> {

}
