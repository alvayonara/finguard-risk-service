package com.alvayonara.finguardriskservice.summary.spending;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingSummaryResponse {

  private String monthKey;
  private BigDecimal totalIncome;
  private BigDecimal totalExpense;
  private String topCategory;
  private List<CategoryAmount> categoryBreakdown;
  private SpendingComparison comparison;
}
