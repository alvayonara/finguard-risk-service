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
public class BudgetExceededRule implements RiskRule {
  @Autowired private BudgetConfigRepository budgetConfigRepository;

  @Override
  public String name() {
    return RuleConstants.BUDGET_EXCEEDED;
  }

  @Override
  public List<String> requiredFeatures() {
    return List.of(FeatureConstants.CATEGORY_MONTHLY_EXPENSE);
  }

  @Override
  public Mono<Void> evaluate(RiskContext context) {
    String category = (String) context.getFeatures().get("latest_category");
    if (Objects.isNull(category)) {
      return Mono.empty();
    }
    BigDecimal expense =
        (BigDecimal) context.getFeatures().get(FeatureConstants.CATEGORY_MONTHLY_EXPENSE);
    return budgetConfigRepository
        .findByUserIdAndCategory(context.getUserId(), category)
        .flatMap(
            config -> {
              if (expense.compareTo(config.getMonthlyLimit()) > 0) {
                RiskSignal signal = new RiskSignal();
                signal.setUserId(context.getUserId());
                signal.setSignalType(name());
                signal.setSeverity("HIGH");
                signal.setDetectedAt(LocalDateTime.now());
                signal.setMetadata(buildMetadata(expense, config.getMonthlyLimit()));
                context.getSignals().add(signal);
              }
              return Mono.empty();
            });
  }

  private String buildMetadata(BigDecimal expense, BigDecimal limit) {
    return """
                {
                  "expense": %s,
                  "limit": %s
                }
                """
        .formatted(expense, limit);
  }
}
