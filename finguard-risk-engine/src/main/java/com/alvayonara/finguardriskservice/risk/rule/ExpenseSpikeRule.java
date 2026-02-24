package com.alvayonara.finguardriskservice.risk.rule;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.risk.feature.config.FeatureConstants;
import com.alvayonara.finguardriskservice.risk.rule.config.RiskRule;
import com.alvayonara.finguardriskservice.risk.rule.config.RiskRuleConfigService;
import com.alvayonara.finguardriskservice.risk.rule.config.RuleConstants;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ExpenseSpikeRule implements RiskRule {
  @Autowired private RiskRuleConfigService riskRuleConfigService;

  @Override
  public String name() {
    return RuleConstants.EXPENSE_SPIKE;
  }

  @Override
  public List<String> requiredFeatures() {
    return List.of(FeatureConstants.AVG_EXPENSE_30D);
  }

  @Override
  public Mono<Void> evaluate(RiskContext context) {
    return riskRuleConfigService
        .get(name())
        .flatMap(
            config -> {
              if (!config.getEnabled()) {
                return Mono.empty();
              }
              BigDecimal avg30d =
                  (BigDecimal) context.getFeatures().get(FeatureConstants.AVG_EXPENSE_30D);
              if (Objects.isNull(avg30d) || avg30d.compareTo(BigDecimal.ZERO) == 0) {
                return Mono.empty();
              }
              BigDecimal todayExpense = (BigDecimal) context.getFeatures().get("latest_expense");
              if (Objects.isNull(todayExpense)) {
                return Mono.empty();
              }
              BigDecimal threshold = config.getThresholdValue();
              boolean spike = todayExpense.compareTo(avg30d.multiply(threshold)) > 0;
              if (spike) {
                RiskSignal signal = new RiskSignal();
                signal.setUserId(context.getUserId());
                signal.setSignalType(name());
                signal.setSeverity(config.getSeverity());
                signal.setDetectedAt(LocalDateTime.now());
                Map<String, Object> meta = buildMetadata(avg30d, todayExpense, threshold);
                signal.setMetadata(JsonUtil.toJson(meta));
                context.getSignals().add(signal);
              }
              return Mono.empty();
            });
  }

  private Map<String, Object> buildMetadata(
      BigDecimal avg30d, BigDecimal todayExpense, BigDecimal threshold) {
    return Map.of(
        "avgExpense30d", avg30d,
        "todayExpense", todayExpense,
        "threshold", threshold);
  }
}
