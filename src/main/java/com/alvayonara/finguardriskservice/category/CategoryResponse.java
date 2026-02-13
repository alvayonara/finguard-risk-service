package com.alvayonara.finguardriskservice.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
  private Long id;
  private String name;
  private String type;
  private String icon;
  private String color;
  private Boolean isDefault;
}
