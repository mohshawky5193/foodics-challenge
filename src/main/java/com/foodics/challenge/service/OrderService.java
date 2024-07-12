package com.foodics.challenge.service;

import com.foodics.challenge.exception.InsufficientIngredientsException;
import com.foodics.challenge.model.entity.Ingredient;
import com.foodics.challenge.model.entity.Order;
import com.foodics.challenge.model.entity.OrderDetail;
import com.foodics.challenge.model.entity.OrderDetailsKey;
import com.foodics.challenge.model.entity.Product;
import com.foodics.challenge.model.request.OrderRequest;
import com.foodics.challenge.model.request.ProductRequest;
import com.foodics.challenge.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

@Service
@Transactional
@RequestScope
public class OrderService {

  private static final String INGREDIENTS_MISSING_SUBJECT = "Some Ingredients are needed";
  private static final String MERCHANT_EMAIL = "mohcufe@gmail.com";
  private final ProductService productService;
  private final IngredientService ingredientService;
  private final OrderRepository orderRepository;

  private final EmailService emailService;

  private final List<String> ingredientsNearToGetOutOfStock= new ArrayList<>();

  private static final String MAIL_TEMPLATE = "We want to buy %s";

  public OrderService(ProductService productService, IngredientService ingredientService,
      OrderRepository orderRepository, EmailService emailService) {
    this.productService = productService;
    this.ingredientService = ingredientService;
    this.orderRepository = orderRepository;
    this.emailService = emailService;
  }

  public void order(OrderRequest orderRequest){
    List<Long> productIds = orderRequest.getProducts().stream().map(ProductRequest::getProductId).toList();
    Map<Long,Integer> productIdToQuantityMap = orderRequest.getProducts().stream().map(productRequest -> Map.entry(productRequest.getProductId(),productRequest.getQuantity())).collect(Collectors.toMap(
        Entry::getKey, Entry::getValue));

    List<Product> productsOrdered = productService.getAllProductsByIdIn(productIds);

    List<Ingredient> ingredients = new ArrayList<>();
    productsOrdered.forEach(product -> product.getProductIngredients().forEach(productIngredient -> {
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
    ingredientService.saveAllIngredients(ingredients);

    Order order = createOrderWithOrderDetails(productsOrdered, productIdToQuantityMap);

    orderRepository.save(order);

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

  private static Order createOrderWithOrderDetails(List<Product> productsOrdered,
      Map<Long, Integer> productIdToQuantityMap) {
    Order order = new Order();
    order.setCreatedAt(LocalDateTime.now());
    List<OrderDetail> orderDetails = new ArrayList<>();
    productsOrdered.forEach(product -> {
      OrderDetail orderDetail = new OrderDetail();
      orderDetail.setProduct(product);
      orderDetail.setQuantity(productIdToQuantityMap.get(product.getId()));
      orderDetail.setOrder(order);
      orderDetail.setOrderDetailsKey(new OrderDetailsKey());
      orderDetails.add(orderDetail);
    });
    order.setOrderDetails(orderDetails);
    return order;
  }
}