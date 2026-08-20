package com.foodics.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodics.challenge.model.entity.Ingredient;
import com.foodics.challenge.model.entity.Product;
import com.foodics.challenge.model.entity.Restaurant;
import com.foodics.challenge.model.entity.Role;
import com.foodics.challenge.model.entity.Supplier;
import com.foodics.challenge.model.entity.User;
import com.foodics.challenge.repository.IngredientRepository;
import com.foodics.challenge.repository.ProductRepository;
import com.foodics.challenge.repository.RestaurantRepository;
import com.foodics.challenge.repository.SupplierRepository;
import com.foodics.challenge.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class SeedDataTest {

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private SupplierRepository supplierRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private IngredientRepository ingredientRepository;

  @Test
  void oneRestaurantOwnsBothSeededProducts() {
    List<Restaurant> restaurants = restaurantRepository.findAll();
    assertEquals(1, restaurants.size());
    Restaurant restaurant = restaurants.get(0);

    List<Product> products = productRepository.findAll();
    assertEquals(2, products.size());
    assertTrue(products.stream()
        .allMatch(product -> product.getRestaurant().getId().equals(restaurant.getId())));
  }

  @Test
  void oneSupplierSuppliesAllFourSeededIngredients() {
    List<Supplier> suppliers = supplierRepository.findAll();
    assertEquals(1, suppliers.size());
    Supplier supplier = suppliers.get(0);

    List<Ingredient> ingredients = ingredientRepository.findAll();
    assertEquals(4, ingredients.size());
    assertTrue(ingredients.stream()
        .allMatch(ingredient -> ingredient.getSupplier().getId().equals(supplier.getId())));
  }

  @Test
  void exactlyOneUserPerRole() {
    List<User> users = userRepository.findAll();
    assertEquals(3, users.size());
    for (Role role : Role.values()) {
      assertEquals(1, users.stream().filter(user -> user.getRole() == role).count());
    }
  }
}
