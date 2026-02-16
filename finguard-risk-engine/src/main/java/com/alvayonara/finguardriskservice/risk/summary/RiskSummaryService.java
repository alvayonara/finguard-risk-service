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
    Mono<List<RiskSignal>> activeSignalsMono =
        riskSignalRepository.findRecentByUserId(userId).collectList();
    Mono<RiskSignal> mostRecentMono = riskSignalRepository.findMostRecentByUserId(userId);

    return Mono.zip(activeSignalsMono, mostRecentMono.defaultIfEmpty(new RiskSignal()))
        .map(tuple -> buildSummary(tuple.getT1(), tuple.getT2()));
  }

  private RiskSummaryResponse buildSummary(List<RiskSignal> activeSignals, RiskSignal mostRecent) {
    if (activeSignals.isEmpty()) {
      return RiskSummaryResponse.builder()
          .level(RiskLevelConstants.LOW)
          .score(RiskLevelConstants.SCORE_LOW)
          .color(RiskLevelConstants.GREEN)
          .topInsightKey(INSIGHT_STABLE)
          .recommendationKey(REC_STABLE)
          .signalsCount(0)
          .lastDetectedAt(mostRecent.getDetectedAt())
          .build();
    }
    RiskSignal worst =
        activeSignals.stream()
            .max(Comparator.comparingInt(s -> severityRank(s.getSeverity())))
            .orElse(activeSignals.getFirst());
    return RiskSummaryResponse.builder()
        .level(worst.getSeverity())
        .score(scoreOf(worst.getSeverity()))
        .color(colorOf(worst.getSeverity()))
        .topInsightKey(mapInsightKey(worst.getSignalType()))
        .recommendationKey(mapRecommendationKey(worst.getSignalType()))
        .topSignalType(worst.getSignalType())
        .signalsCount(activeSignals.size())
        .lastDetectedAt(worst.getDetectedAt())
        .build();
  }
}
