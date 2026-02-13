package com.alvayonara.finguardriskservice.budget.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BudgetRequest {
  private Long categoryId;
  private BigDecimal monthlyLimit;
}
