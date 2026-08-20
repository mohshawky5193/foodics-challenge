package com.foodics.challenge.model.response;

import java.util.List;

public record PagedResult<T>(List<T> items, PaginationInfo paginationInfo) {

}
