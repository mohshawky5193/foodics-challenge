package com.foodics.challenge.model.request;

import java.util.List;

public record OrderRequest(List<ProductRequest> products) {

}
