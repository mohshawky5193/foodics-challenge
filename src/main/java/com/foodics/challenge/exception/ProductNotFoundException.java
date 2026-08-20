package com.foodics.challenge.exception;

import java.util.List;
import java.util.stream.Collectors;

public class ProductNotFoundException extends RuntimeException {

  private final List<Long> missingProductIds;

  public ProductNotFoundException(List<Long> missingProductIds) {
    super("Product(s) not found: " + missingProductIds.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(", ")));
    this.missingProductIds = missingProductIds;
  }

  public List<Long> getMissingProductIds() {
    return missingProductIds;
  }
}
