package com.alvayonara.finguardriskservice.category;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CategoryExceptionHandler {

  @ExceptionHandler(DuplicateCategoryException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateCategoryException(
      DuplicateCategoryException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "DUPLICATE_CATEGORY", "message", ex.getMessage()));
  }
}
