package com.alvayonara.finguardriskservice.dashboard;

import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
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
        private String type;
        private Double amount;
        private String category;
        private String occurredAt;
    }
}
