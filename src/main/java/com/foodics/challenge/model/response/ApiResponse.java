package com.foodics.challenge.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String status, T data, PaginationInfo paginationInfo) {

}
