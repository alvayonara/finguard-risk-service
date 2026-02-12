package com.alvayonara.finguardriskservice.budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("budget_config")
public class BudgetConfig {
  @Id private Long id;
  private Long userId;
  private String category;
  private BigDecimal monthlyLimit;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
