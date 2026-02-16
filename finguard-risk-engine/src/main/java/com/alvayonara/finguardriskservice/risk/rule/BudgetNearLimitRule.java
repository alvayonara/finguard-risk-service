package com.alvayonara.finguardriskservice.risk.rule;

import com.alvayonara.finguardriskservice.budget.BudgetConfigRepository;
import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.risk.feature.config.FeatureConstants;
import com.alvayonara.finguardriskservice.risk.rule.config.RiskRule;
import com.alvayonara.finguardriskservice.risk.rule.config.RuleConstants;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BudgetNearLimitRule implements RiskRule {
  @Autowired private BudgetConfigRepository budgetConfigRepository;
  private static final BigDecimal THRESHOLD = new BigDecimal("0.8");

  @Override
  public String name() {
    return RuleConstants.BUDGET_NEAR_LIMIT;
  }

  @Override
  public List<String> requiredFeatures() {
    return List.of(FeatureConstants.CATEGORY_MONTHLY_EXPENSE);
  }

  @Override
  public Mono<Void> evaluate(RiskContext context) {
    Long categoryId = (Long) context.getFeatures().get("latest_category_id");
    BigDecimal expense =
        (BigDecimal) context.getFeatures().get(FeatureConstants.CATEGORY_MONTHLY_EXPENSE);
    if (Objects.isNull(categoryId) || Objects.isNull(expense)) {
      return Mono.empty();
    }
    return budgetConfigRepository
        .findByUserIdAndCategoryId(context.getUserId(), categoryId)
        .flatMap(
            config -> {
              BigDecimal thresholdValue = config.getMonthlyLimit().multiply(THRESHOLD);
              if (expense.compareTo(thresholdValue) >= 0
                  && expense.compareTo(config.getMonthlyLimit()) <= 0) {
                RiskSignal signal = new RiskSignal();
                signal.setUserId(context.getUserId());
                signal.setSignalType(name());
                signal.setSeverity("MEDIUM");
                signal.setDetectedAt(LocalDateTime.now());
                context.getSignals().add(signal);
              }
              return Mono.empty();
            });
  }
}
