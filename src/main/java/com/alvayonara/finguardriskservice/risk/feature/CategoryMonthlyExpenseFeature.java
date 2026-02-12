package com.alvayonara.finguardriskservice.risk.feature;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.risk.feature.config.FeatureConstants;
import com.alvayonara.finguardriskservice.risk.feature.config.RiskFeature;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CategoryMonthlyExpenseFeature implements RiskFeature {
  @Autowired private TransactionRepository transactionRepository;

  @Override
  public String name() {
    return FeatureConstants.CATEGORY_MONTHLY_EXPENSE;
  }

  @Override
  public Mono<Void> compute(RiskContext riskContext) {
    String category = (String) riskContext.getFeatures().get("latest_category");
    if (Objects.isNull(category)) {
      return Mono.empty();
    }
    YearMonth month = YearMonth.parse(riskContext.getMonthKey());
    LocalDate start = month.atDay(1);
    LocalDate end = month.plusMonths(1).atDay(1);
    return transactionRepository
        .sumExpenseByCategoryAndPeriod(riskContext.getUserId(), category, start, end)
        .defaultIfEmpty(BigDecimal.ZERO)
        .doOnNext(sum -> riskContext.getFeatures().put(name(), sum))
        .then();
  }
}
