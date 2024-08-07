package com.foodics.challenge.service;

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
public class OrderService {


  private final ProductService productService;
  private final IngredientService ingredientService;
  private final OrderRepository orderRepository;

  public OrderService(ProductService productService, IngredientService ingredientService,
      OrderRepository orderRepository) {
    this.productService = productService;
    this.ingredientService = ingredientService;
    this.orderRepository = orderRepository;
  }

  public void order(OrderRequest orderRequest){
    List<Long> productIds = orderRequest.getProducts().stream().map(ProductRequest::getProductId).toList();
    Map<Long,Integer> productIdToQuantityMap = orderRequest.getProducts().stream().map(productRequest -> Map.entry(productRequest.getProductId(),productRequest.getQuantity())).collect(Collectors.toMap(
        Entry::getKey, Entry::getValue));

    List<Product> productsOrdered = productService.getAllProductsByIdIn(productIds);

    ingredientService.updateIngredientsStock(productsOrdered,productIdToQuantityMap);

    Order order = createOrderWithOrderDetails(productsOrdered, productIdToQuantityMap);

    orderRepository.save(order);



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