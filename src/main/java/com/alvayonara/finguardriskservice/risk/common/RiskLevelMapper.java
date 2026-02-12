package com.alvayonara.finguardriskservice.risk.common;

import static com.alvayonara.finguardriskservice.risk.level.RiskLevelConstants.*;

public final class RiskLevelMapper {

  public static int scoreOf(String level) {
    return switch (level) {
      case HIGH -> SCORE_HIGH;
      case MEDIUM -> SCORE_MEDIUM;
      default -> SCORE_LOW;
    };
  }

  public static String colorOf(String level) {
    return switch (level) {
      case HIGH -> RED;
      case MEDIUM -> ORANGE;
      default -> GREEN;
    };
  }

  public static int severityRank(String s) {
    return switch (s) {
      case HIGH -> 3;
      case MEDIUM -> 2;
      default -> 1;
    };
  }
}
