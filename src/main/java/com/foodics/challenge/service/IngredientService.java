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
  private static final String MERCHANT_EMAIL = "mohcufe@gmail.com";

  private static final String MAIL_TEMPLATE = "We want to buy %s";


  public IngredientService(IngredientRepository ingredientRepository, EmailService emailService) {
    this.ingredientRepository = ingredientRepository;
    this.emailService = emailService;
  }

  public void updateIngredientsStock(List<Product> products, Map<Long,Integer> productIdToQuantityMap){
    List<Ingredient> ingredients = new ArrayList<>();
    List<String> ingredientsNearToGetOutOfStock = new ArrayList<>();
    products.forEach(product -> product.getProductIngredients().forEach(productIngredient -> {
      Ingredient ingredient = productIngredient.getIngredient();
      int consumedAmount = ingredient.getConsumedAmountInGrams() == null ? 0:ingredient.getConsumedAmountInGrams();
      int calculatedConsumedAmount = consumedAmount+productIdToQuantityMap.get(product.getId())*productIngredient.getAmountInGrams();
      ingredients.add(ingredient);
      if(consumedAmount <= ingredient.getAmountInGrams()*0.5 &&  calculatedConsumedAmount> ingredient.getAmountInGrams()*0.5){
        ingredientsNearToGetOutOfStock.add(ingredient.getName());
      }else if(calculatedConsumedAmount > ingredient.getAmountInGrams()){
        throw new InsufficientIngredientsException();
      }
      ingredient.setConsumedAmountInGrams(consumedAmount+productIngredient.getAmountInGrams()*productIdToQuantityMap.get(product.getId()));
    }));
    ingredientRepository.saveAll(ingredients);

    if(!ingredientsNearToGetOutOfStock.isEmpty()){
      String messageToBeSent = createMessageToBeSent(ingredientsNearToGetOutOfStock);
      emailService.sendEmail(INGREDIENTS_MISSING_SUBJECT,MERCHANT_EMAIL,messageToBeSent);
    }
  }

  private String createMessageToBeSent(List<String> ingredientsNearToGetOutOfStock) {
    if(ingredientsNearToGetOutOfStock.size() == 1){
      return String.format(MAIL_TEMPLATE,ingredientsNearToGetOutOfStock.get(0));
    }else {
      StringBuilder messageBuilder = new StringBuilder();
      for(int i=0;i<ingredientsNearToGetOutOfStock.size()-1;i++){
        messageBuilder.append(ingredientsNearToGetOutOfStock.get(i));
        if(i != ingredientsNearToGetOutOfStock.size()-2){
          messageBuilder.append(", ");
        }
      }
      messageBuilder.append(" and ");
      messageBuilder.append(ingredientsNearToGetOutOfStock.get(ingredientsNearToGetOutOfStock.size()-1));
      return String.format(MAIL_TEMPLATE,messageBuilder);
    }
  }

}
