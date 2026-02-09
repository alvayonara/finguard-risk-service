package com.alvayonara.finguardriskservice.dashboard;

import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryResponse;
import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryService;
import com.alvayonara.finguardriskservice.summary.MonthlySummaryRepository;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DashboardService {
    @Autowired
    private RiskSummaryService riskSummaryService;
    @Autowired
    private MonthlySummaryRepository monthlySummaryRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Mono<DashboardResponse> getDashboard(Long userId) {
        Mono<RiskSummaryResponse> riskMono = riskSummaryService.getSummary(userId);
        Mono<DashboardResponse.MonthSummary> monthMono = monthlySummaryRepository.findLatestByUserId(userId)
                .map(ms -> DashboardResponse.MonthSummary.builder()
                        .monthKey(ms.getMonthKey())
                        .totalIncome(ms.getTotalIncome().doubleValue())
                        .totalExpense(ms.getTotalExpense().doubleValue())
                        .build())
                .defaultIfEmpty(DashboardResponse.MonthSummary.builder().build());
        Mono<List<DashboardResponse.RecentTransactionItem>> txMono = transactionRepository.findRecentByUserId(userId)
                .map(tx -> DashboardResponse.RecentTransactionItem.builder()
                        .type(tx.getType())
                        .amount(tx.getAmount().doubleValue())
                        .category(tx.getCategory())
                        .occurredAt(tx.getOccurredAt().toString())
                        .build())
                .collectList();
        return Mono.zip(riskMono, monthMono, txMono)
                .map(tuple -> DashboardResponse.builder()
                        .financialHealth(tuple.getT1())
                        .monthSummary(tuple.getT2())
                        .recentTransactions(tuple.getT3())
                        .build());
    }
}
