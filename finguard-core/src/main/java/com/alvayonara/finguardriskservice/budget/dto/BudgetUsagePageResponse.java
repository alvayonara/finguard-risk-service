package com.alvayonara.finguardriskservice.budget.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetUsagePageResponse {
  private List<BudgetUsageResponse> budgets;
  private String nextCursorTime;
  private Long nextCursorId;
}
