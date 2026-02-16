package com.alvayonara.finguardriskservice.category;

public class DuplicateCategoryException extends RuntimeException {
  public DuplicateCategoryException(String name, String type) {
    super("Category with name '" + name + "' and type '" + type + "' already exists");
  }
}
