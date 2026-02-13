package com.alvayonara.finguardriskservice.category;

import lombok.Data;

@Data
public class CategoryRequest {
  private String name;
  private String type;
  private String icon;
  private String color;
}
