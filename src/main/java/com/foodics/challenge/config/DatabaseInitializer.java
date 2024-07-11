package com.foodics.challenge.config;

import com.foodics.challenge.model.entity.Ingredient;
import com.foodics.challenge.model.entity.Product;
import com.foodics.challenge.model.entity.ProductIngredient;
import com.foodics.challenge.model.entity.ProductIngredientKey;
import com.foodics.challenge.repository.IngredientRepository;
import com.foodics.challenge.repository.ProductIngredientRepository;
import com.foodics.challenge.repository.ProductRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DatabaseInitializer implements CommandLineRunner {

  private final ProductRepository productRepository;
  private final IngredientRepository ingredientRepository;

  private final ProductIngredientRepository productIngredientRepository;

  public DatabaseInitializer(ProductRepository productRepository,
      IngredientRepository ingredientRepository,
      ProductIngredientRepository productIngredientRepository) {
    this.productRepository = productRepository;
    this.ingredientRepository = ingredientRepository;
    this.productIngredientRepository = productIngredientRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    Product burger = createProduct("Burger");

    Product chickenBurger = createProduct("Chicken Burger");


    List<Product> products = productRepository.saveAll(List.of(burger,chickenBurger));



    Ingredient beef = createIngredient("Beef",20000);
    Ingredient chicken = createIngredient("Chicken",20000);
    Ingredient cheese = createIngredient("Cheese",5000);
    Ingredient onion =createIngredient("Onion",1000);


    ingredientRepository.saveAll(List.of(beef,chicken,cheese,onion));

    ProductIngredient productIngredient1= createProductIngredient(burger,beef,150);
    ProductIngredient productIngredient2= createProductIngredient(burger,cheese,30);
    ProductIngredient productIngredient3= createProductIngredient(burger,onion,20);

    ProductIngredient productIngredient4= createProductIngredient(chickenBurger,chicken,150);
    ProductIngredient productIngredient5= createProductIngredient(chickenBurger,cheese,30);
    ProductIngredient productIngredient6= createProductIngredient(chickenBurger,onion,20);

    productIngredientRepository.saveAll(List.of(productIngredient1,productIngredient2,productIngredient3,productIngredient4,productIngredient5,productIngredient6));

  }

  private  Product createProduct(String name) {
    Product product = new Product();
    product.setName(name);
    return product;
  }

  private Ingredient createIngredient(String name,Integer amountInGrams){
    Ingredient ingredient = new Ingredient();
    ingredient.setName(name);
    ingredient.setAmountInGrams(amountInGrams);
    return ingredient;
  }

  private ProductIngredient createProductIngredient(Product product,Ingredient ingredient,Integer amountInGrams){
    ProductIngredientKey productIngredientKey = createProductIngredientKey(product.getId(),ingredient.getId());
    ProductIngredient productIngredient = new ProductIngredient();
    productIngredient.setProductIngredientKey(productIngredientKey);
    productIngredient.setAmountInGrams(amountInGrams);
    productIngredient.setIngredient(ingredient);
    productIngredient.setProduct(product);
    return productIngredient;
  }

  private ProductIngredientKey createProductIngredientKey(Long productId, Long ingredientId) {
    ProductIngredientKey productIngredientKey = new ProductIngredientKey();
    productIngredientKey.setIngredientId(productId);
    productIngredientKey.setIngredientId(ingredientId);
    return productIngredientKey;
  }


}
