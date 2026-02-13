package com.alvayonara.finguardriskservice.category;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("categories")
public class Category {
  @Id private Long id;
  private Long userId;
  private String name;
  private String type;
  private String icon;
  private String color;
  private Boolean isDefault;
  private LocalDateTime createdAt;
}
