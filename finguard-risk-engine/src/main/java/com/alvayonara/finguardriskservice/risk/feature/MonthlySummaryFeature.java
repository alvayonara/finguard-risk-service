package com.alvayonara.finguardriskservice.risk.feature;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.risk.feature.config.FeatureConstants;
import com.alvayonara.finguardriskservice.risk.feature.config.RiskFeature;
import com.alvayonara.finguardriskservice.spending.summary.TypeSumProjection;
import com.alvayonara.finguardriskservice.summary.MonthlySummary;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MonthlySummaryFeature implements RiskFeature {
  @Autowired private TransactionRepository transactionRepository;

  @Override
  public String name() {
    return FeatureConstants.MONTHLY_SUMMARY;
  }

  @Override
  public Mono<Void> compute(RiskContext context) {
    YearMonth monthKey = YearMonth.parse(context.getMonthKey());
    LocalDate startOfMonth = monthKey.atDay(1);
    LocalDate endOfMonth = monthKey.atEndOfMonth().plusDays(1);
    return transactionRepository
        .sumByType(context.getUserId(), startOfMonth, endOfMonth)
        .collectMap(TypeSumProjection::type, TypeSumProjection::total)
        .doOnNext(
            totals -> {
              BigDecimal income =
                  totals.getOrDefault(TransactionType.INCOME.name(), BigDecimal.ZERO);
              BigDecimal expense =
                  totals.getOrDefault(TransactionType.EXPENSE.name(), BigDecimal.ZERO);
              MonthlySummary summary =
                  MonthlySummary.builder()
                      .userId(context.getUserId())
                      .monthKey(context.getMonthKey())
                      .totalIncome(income)
                      .totalExpense(expense)
                      .build();
              context.getFeatures().put(name(), summary);
            })
        .then();
  }
}
