package com.foodics.challenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.foodics.challenge.exception.InsufficientIngredientsException;
import com.foodics.challenge.model.entity.Ingredient;
import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.repository.IngredientRepository;
import com.foodics.challenge.repository.OrderRepository;
import com.foodics.challenge.util.OrderRequestUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import(value = {OrderService.class, ProductService.class,IngredientService.class})
public class OrderServiceIntegrationTest {


  @Autowired
  private OrderService orderService;

  @MockitoBean
  private EmailService emailService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private IngredientRepository ingredientRepository;


  @Test
  void saveOrderNoEmail(){

    OrderRequest orderRequest = OrderRequestUtils.noEmailOrderRequest();

    orderService.order(orderRequest);

    verify(emailService,never()).sendEmail(anyString(),anyString(),anyString());

    assertEquals(orderRepository.findAll().size(),1);

    List<Ingredient> ingredients = ingredientRepository.findAll();

    assertTrue(ingredients.stream().allMatch(ingredient -> ingredient.getConsumedAmountInGrams() != null));

  }

  @Test
  void saveOrderEmail(){
    OrderRequest orderRequest = OrderRequestUtils.orderRequestCausingEmail();

    orderService.order(orderRequest);

    verify(emailService).sendEmail(anyString(),anyString(),anyString());

    assertEquals(orderRepository.findAll().size(),1);

    List<Ingredient> ingredients = ingredientRepository.findAll();

    assertEquals(ingredients.stream().filter(ingredient -> ingredient.getConsumedAmountInGrams() != null).count(),3L);
  }

  @Test
  void rejectOrderExceedingStock(){
    OrderRequest orderRequest = OrderRequestUtils.orderRequestInsufficientIngredients();

    assertThrows(InsufficientIngredientsException.class, () -> orderService.order(orderRequest));

    verify(emailService,never()).sendEmail(anyString(),anyString(),anyString());
  }

  @Test
  void saveOrderEmailOnceWithMultipleOrderRequests(){
    OrderRequest orderRequest = OrderRequestUtils.orderRequestCausingEmail();


    OrderRequest orderRequest2 = OrderRequestUtils.noEmailOrderRequest();

    orderService.order(orderRequest);
    orderService.order(orderRequest2);

    verify(emailService).sendEmail(anyString(),anyString(),anyString());

    assertEquals(orderRepository.findAll().size(),2);

    List<Ingredient> ingredients = ingredientRepository.findAll();

    assertTrue(ingredients.stream().allMatch(ingredient -> ingredient.getConsumedAmountInGrams() != null));
  }
}
