package com.foodics.challenge.model.response;

public record ApiResponse<T>(String code, String status, T data) {

}
