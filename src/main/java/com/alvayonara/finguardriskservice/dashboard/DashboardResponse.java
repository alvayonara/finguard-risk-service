package com.alvayonara.finguardriskservice.dashboard;

import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryResponse;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
  private String state;
  private RiskSummaryResponse financialHealth;
  private MonthSummary monthSummary;
  private List<RecentTransactionItem> recentTransactions;

  @Data
  @Builder
  public static class MonthSummary {
    private String monthKey;
    private Double totalIncome;
    private Double totalExpense;
  }

  @Data
  @Builder
  public static class RecentTransactionItem {
    private Long id;
    private TransactionType type;
    private Double amount;
    private Long categoryId;
    private String category;
    private String occurredAt;
  }
}
