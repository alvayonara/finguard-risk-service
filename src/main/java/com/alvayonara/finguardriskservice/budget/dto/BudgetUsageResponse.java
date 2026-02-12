package com.alvayonara.finguardriskservice.budget.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetUsageResponse {
  private String category;
  private BigDecimal monthlyLimit;
  private BigDecimal spent;
  private BigDecimal remaining;
  private BigDecimal percentageUsed;
}
