package com.foodics.challenge.service;

import com.foodics.challenge.exception.InsufficientIngredientsException;
import com.foodics.challenge.model.entity.Ingredient;
import com.foodics.challenge.model.entity.Product;
import com.foodics.challenge.repository.IngredientRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IngredientService {

  private final IngredientRepository ingredientRepository;
  private final EmailService emailService;

  private static final String INGREDIENTS_MISSING_SUBJECT = "Some Ingredients are needed";

  private static final String MAIL_TEMPLATE = "We want to buy %s";


  public IngredientService(IngredientRepository ingredientRepository, EmailService emailService) {
    this.ingredientRepository = ingredientRepository;
    this.emailService = emailService;
  }

  public void updateIngredientsStock(List<Product> products, Map<Long,Integer> productIdToQuantityMap){
    List<Ingredient> ingredients = new ArrayList<>();
    List<Ingredient> ingredientsNearToGetOutOfStock = new ArrayList<>();
    products.forEach(product -> product.getProductIngredients().forEach(productIngredient -> {
      Ingredient ingredient = productIngredient.getIngredient();
      int consumedAmount = ingredient.getConsumedAmountInGrams() == null ? 0:ingredient.getConsumedAmountInGrams();
      int calculatedConsumedAmount = consumedAmount+productIdToQuantityMap.get(product.getId())*productIngredient.getAmountInGrams();
      ingredients.add(ingredient);
      if(calculatedConsumedAmount > ingredient.getAmountInGrams()){
        throw new InsufficientIngredientsException();
      }
      if(consumedAmount <= ingredient.getAmountInGrams()*0.5 &&  calculatedConsumedAmount> ingredient.getAmountInGrams()*0.5){
        ingredientsNearToGetOutOfStock.add(ingredient);
      }
      ingredient.setConsumedAmountInGrams(consumedAmount+productIngredient.getAmountInGrams()*productIdToQuantityMap.get(product.getId()));
    }));
    ingredientRepository.saveAll(ingredients);

    if(!ingredientsNearToGetOutOfStock.isEmpty()){
      String messageToBeSent = createMessageToBeSent(ingredientsNearToGetOutOfStock.stream().map(Ingredient::getName).toList());
      String supplierEmail = ingredientsNearToGetOutOfStock.get(0).getSupplier().getEmail();
      emailService.sendEmail(INGREDIENTS_MISSING_SUBJECT,supplierEmail,messageToBeSent);
    }
  }

  private String createMessageToBeSent(List<String> ingredientNamesNearToGetOutOfStock) {
    if(ingredientNamesNearToGetOutOfStock.size() == 1){
      return String.format(MAIL_TEMPLATE,ingredientNamesNearToGetOutOfStock.get(0));
    }else {
      StringBuilder messageBuilder = new StringBuilder();
      for(int i=0;i<ingredientNamesNearToGetOutOfStock.size()-1;i++){
        messageBuilder.append(ingredientNamesNearToGetOutOfStock.get(i));
        if(i != ingredientNamesNearToGetOutOfStock.size()-2){
          messageBuilder.append(", ");
        }
      }
      messageBuilder.append(" and ");
      messageBuilder.append(ingredientNamesNearToGetOutOfStock.get(ingredientNamesNearToGetOutOfStock.size()-1));
      return String.format(MAIL_TEMPLATE,messageBuilder);
    }
  }

}
