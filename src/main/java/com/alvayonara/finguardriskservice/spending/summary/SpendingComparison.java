package com.alvayonara.finguardriskservice.spending.summary;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpendingComparison {
  private BigDecimal previousExpense;
  private BigDecimal percentageChange;
  private String trend;
}
