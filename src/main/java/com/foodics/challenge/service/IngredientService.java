package com.foodics.challenge.service;

import com.foodics.challenge.model.entity.Ingredient;
import com.foodics.challenge.repository.IngredientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IngredientService {

  private final IngredientRepository ingredientRepository;

  public IngredientService(IngredientRepository ingredientRepository) {
    this.ingredientRepository = ingredientRepository;
  }

  public void saveAllIngredients(List<Ingredient> ingredients){
    ingredientRepository.saveAll(ingredients);
  }
}
