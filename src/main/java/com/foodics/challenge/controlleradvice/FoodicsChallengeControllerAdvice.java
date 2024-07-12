package com.foodics.challenge.controlleradvice;

import com.foodics.challenge.exception.InsufficientIngredientsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class FoodicsChallengeControllerAdvice {

  @ExceptionHandler(InsufficientIngredientsException.class)
  public ResponseEntity<String> handleException(InsufficientIngredientsException exception){
    return new ResponseEntity<>("Insufficient ingredients for your order",HttpStatus.BAD_REQUEST);
  }
}
