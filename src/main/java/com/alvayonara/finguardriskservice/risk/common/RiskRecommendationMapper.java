package com.alvayonara.finguardriskservice.risk.common;

import static com.alvayonara.finguardriskservice.risk.rule.RuleConstants.EXPENSE_SPIKE;
import static com.alvayonara.finguardriskservice.risk.rule.RuleConstants.NEGATIVE_CASH_FLOW;

public class RiskRecommendationMapper {
  public static final String REC_NEGATIVE_CASH_FLOW = "REC_NEGATIVE_CASH_FLOW";
  public static final String REC_EXPENSE_SPIKE = "REC_EXPENSE_SPIKE";
  public static final String REC_STABLE = "REC_STABLE";

  public static String mapRecommendationKey(String type) {
    return switch (type) {
      case NEGATIVE_CASH_FLOW -> REC_NEGATIVE_CASH_FLOW;
      case EXPENSE_SPIKE -> REC_EXPENSE_SPIKE;
      default -> REC_STABLE;
    };
  }
}
