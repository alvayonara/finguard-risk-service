package com.alvayonara.finguardriskservice.budget.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BudgetRequest {
  private String category;
  private BigDecimal monthlyLimit;
}
