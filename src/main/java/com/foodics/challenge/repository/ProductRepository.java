package com.foodics.challenge.repository;

import com.foodics.challenge.model.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
  List<Product> findByIdIn(List<Long> productIds);
}
