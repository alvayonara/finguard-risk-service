package com.alvayonara.finguardriskservice.risk.summary;

import static com.alvayonara.finguardriskservice.risk.common.RiskConstants.*;
import static com.alvayonara.finguardriskservice.risk.insight.RiskInsightConstants.*;
import static com.alvayonara.finguardriskservice.risk.level.RiskLevelConstants.*;
import static com.alvayonara.finguardriskservice.risk.rule.RuleConstants.EXPENSE_SPIKE;
import static com.alvayonara.finguardriskservice.risk.rule.RuleConstants.NEGATIVE_CASH_FLOW;

import com.alvayonara.finguardriskservice.risk.insight.RiskInsightConstants;
import com.alvayonara.finguardriskservice.risk.level.RiskLevelConstants;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RiskSummaryService {
  @Autowired private RiskSignalRepository riskSignalRepository;

  public Mono<RiskSummaryResponse> getSummary(Long userId) {
    return riskSignalRepository.findRecentByUserId(userId).collectList().map(this::buildSummary);
  }

  private RiskSummaryResponse buildSummary(List<RiskSignal> signals) {
    if (signals.isEmpty()) {
      return RiskSummaryResponse.builder()
          .level(RiskLevelConstants.LOW)
          .score(RiskLevelConstants.SCORE_LOW)
          .color(RiskLevelConstants.GREEN)
          .topInsight(RiskInsightConstants.MSG_STABLE)
          .recommendation(REC_STABLE)
          .signalsCount(0)
          .build();
    }
    RiskSignal worst =
        signals.stream()
            .max(Comparator.comparingInt(s -> severityRank(s.getSeverity())))
            .orElse(signals.get(0));
    return RiskSummaryResponse.builder()
        .level(worst.getSeverity())
        .score(scoreOf(worst.getSeverity()))
        .color(colorOf(worst.getSeverity()))
        .topInsight(mapMessage(worst.getSignalType()))
        .recommendation(mapRecommendation(worst.getSignalType()))
        .topSignalType(worst.getSignalType())
        .signalsCount(signals.size())
        .lastDetectedAt(worst.getDetectedAt())
        .build();
  }

  private int severityRank(String s) {
    return switch (s) {
      case HIGH -> 3;
      case MEDIUM -> 2;
      default -> 1;
    };
  }

  private int scoreOf(String s) {
    return switch (s) {
      case HIGH -> SCORE_HIGH;
      case MEDIUM -> SCORE_MEDIUM;
      default -> SCORE_LOW;
    };
  }

  private String colorOf(String s) {
    return switch (s) {
      case HIGH -> RED;
      case MEDIUM -> ORANGE;
      default -> GREEN;
    };
  }

  private String mapMessage(String type) {
    return switch (type) {
      case NEGATIVE_CASH_FLOW -> MSG_NEGATIVE_CASH_FLOW;
      case EXPENSE_SPIKE -> MSG_EXPENSE_SPIKE;
      default -> MSG_GENERIC;
    };
  }

  private String mapRecommendation(String type) {
    return switch (type) {
      case NEGATIVE_CASH_FLOW -> REC_NEGATIVE_CASHFLOW;
      case EXPENSE_SPIKE -> REC_EXPENSE_SPIKE;
      default -> REC_STABLE;
    };
  }
}
