package com.alvayonara.finguardriskservice.risk.common;

import static com.alvayonara.finguardriskservice.risk.rule.config.RuleConstants.EXPENSE_SPIKE;
import static com.alvayonara.finguardriskservice.risk.rule.config.RuleConstants.NEGATIVE_CASH_FLOW;

public final class RiskInsightMapper {
  public static final String INSIGHT_NEGATIVE_CASH_FLOW = "INSIGHT_NEGATIVE_CASH_FLOW";
  public static final String INSIGHT_EXPENSE_SPIKE = "INSIGHT_EXPENSE_SPIKE";
  public static final String INSIGHT_GENERIC = "INSIGHT_GENERIC";
  public static final String INSIGHT_STABLE = "INSIGHT_STABLE";

  public static String mapInsightKey(String type) {
    return switch (type) {
      case NEGATIVE_CASH_FLOW -> INSIGHT_NEGATIVE_CASH_FLOW;
      case EXPENSE_SPIKE -> INSIGHT_EXPENSE_SPIKE;
      default -> INSIGHT_GENERIC;
    };
  }
}
