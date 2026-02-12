package com.alvayonara.finguardriskservice.risk.summary;

import static com.alvayonara.finguardriskservice.risk.common.RiskInsightMapper.INSIGHT_STABLE;
import static com.alvayonara.finguardriskservice.risk.common.RiskInsightMapper.mapInsightKey;
import static com.alvayonara.finguardriskservice.risk.common.RiskLevelMapper.*;
import static com.alvayonara.finguardriskservice.risk.common.RiskRecommendationMapper.REC_STABLE;
import static com.alvayonara.finguardriskservice.risk.common.RiskRecommendationMapper.mapRecommendationKey;

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
          .topInsightKey(INSIGHT_STABLE)
          .recommendationKey(REC_STABLE)
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
        .topInsightKey(mapInsightKey(worst.getSignalType()))
        .recommendationKey(mapRecommendationKey(worst.getSignalType()))
        .topSignalType(worst.getSignalType())
        .signalsCount(signals.size())
        .lastDetectedAt(worst.getDetectedAt())
        .build();
  }
}
