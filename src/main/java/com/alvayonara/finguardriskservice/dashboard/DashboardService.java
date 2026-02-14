package com.alvayonara.finguardriskservice.dashboard;

import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryResponse;
import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryService;
import com.alvayonara.finguardriskservice.summary.MonthlySummaryRepository;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DashboardService {
    @Autowired
    private RiskSummaryService riskSummaryService;
    @Autowired
    private MonthlySummaryRepository monthlySummaryRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Mono<DashboardResponse> getDashboard(Long userId) {
        Mono<DashboardResponse.MonthSummary> monthMono =
                monthlySummaryRepository
                        .findLatestByUserId(userId)
                        .map(
                                ms ->
                                        DashboardResponse.MonthSummary.builder()
                                                .monthKey(ms.getMonthKey())
                                                .totalIncome(ms.getTotalIncome().doubleValue())
                                                .totalExpense(ms.getTotalExpense().doubleValue())
                                                .build())
                        .defaultIfEmpty(
                                DashboardResponse.MonthSummary.builder()
                                        .totalIncome(0.0)
                                        .totalExpense(0.0)
                                        .build());
        Mono<List<DashboardResponse.RecentTransactionItem>> txMono =
                transactionRepository
                        .findRecentWithCategory(userId)
                        .map(
                                tx ->
                                        DashboardResponse.RecentTransactionItem.builder()
                                                .id(tx.id())
                                                .type(tx.type())
                                                .amount(tx.amount().doubleValue())
                                                .categoryId(tx.categoryId())
                                                .category(tx.category())
                                                .occurredAt(tx.occurredAt().toString())
                                                .build())
                        .collectList();
        Mono<RiskSummaryResponse> summaryMono = riskSummaryService.getSummary(userId);
        return Mono.zip(monthMono, txMono, summaryMono)
                .map(tuple -> DashboardResponse.builder()
                        .financialHealth(tuple.getT3())
                        .monthSummary(tuple.getT1())
                        .recentTransactions(tuple.getT2())
                        .build());
    }
}
