package com.alvayonara.finguardriskservice.dashboard;

import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryResponse;
import com.alvayonara.finguardriskservice.risk.summary.RiskSummaryService;
import com.alvayonara.finguardriskservice.spending.summary.TypeSumProjection;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DashboardService {
  @Autowired private RiskSummaryService riskSummaryService;
  @Autowired private TransactionRepository transactionRepository;

  public Mono<DashboardResponse> getDashboard(Long userId) {
    YearMonth currentMonth = YearMonth.now();
    LocalDate startOfMonth = currentMonth.atDay(1);
    LocalDate endOfMonth = currentMonth.atEndOfMonth().plusDays(1);
    Mono<DashboardResponse.MonthSummary> monthMono =
        transactionRepository
            .sumByType(userId, startOfMonth, endOfMonth)
            .collectMap(TypeSumProjection::type, TypeSumProjection::total)
            .map(
                totals -> {
                  BigDecimal income =
                      totals.getOrDefault(TransactionType.INCOME.name(), BigDecimal.ZERO);
                  BigDecimal expense =
                      totals.getOrDefault(TransactionType.EXPENSE.name(), BigDecimal.ZERO);
                  return DashboardResponse.MonthSummary.builder()
                      .monthKey(currentMonth.toString())
                      .totalIncome(income.doubleValue())
                      .totalExpense(expense.doubleValue())
                      .build();
                });
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
    Mono<RiskSummaryResponse> riskMono = riskSummaryService.getSummary(userId);
    return Mono.zip(monthMono, txMono, riskMono)
        .map(
            tuple ->
                DashboardResponse.builder()
                    .financialHealth(tuple.getT3())
                    .monthSummary(tuple.getT1())
                    .recentTransactions(tuple.getT2())
                    .build());
  }
}
