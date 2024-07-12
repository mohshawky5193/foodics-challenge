package com.foodics.challenge.service;

import com.foodics.challenge.model.entity.Product;
import com.foodics.challenge.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

  private final ProductRepository productRepository;


  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public List<Product> getAllProductsByIdIn(List<Long> productIds){
    return productRepository.findByIdIn(productIds);
  }
}
