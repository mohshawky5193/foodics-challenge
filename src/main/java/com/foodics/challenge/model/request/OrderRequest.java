package com.foodics.challenge.model.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(@NotNull Long restaurantId, List<ProductRequest> products) {

}
