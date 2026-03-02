package com.alvayonara.finguardriskservice.risk.detail;

import static com.alvayonara.finguardriskservice.risk.common.RiskInsightMapper.INSIGHT_STABLE;
import static com.alvayonara.finguardriskservice.risk.common.RiskInsightMapper.mapInsightKey;
import static com.alvayonara.finguardriskservice.risk.common.RiskLevelMapper.*;
import static com.alvayonara.finguardriskservice.risk.common.RiskRecommendationMapper.REC_STABLE;
import static com.alvayonara.finguardriskservice.risk.common.RiskRecommendationMapper.mapRecommendationKey;

import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistory;
import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistoryRepository;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import com.alvayonara.finguardriskservice.risk.state.RiskState;
import com.alvayonara.finguardriskservice.risk.state.RiskStateRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RiskDetailService {

  private final RiskStateRepository riskStateRepository;
  private final RiskSignalRepository riskSignalRepository;
  private final RiskLevelHistoryRepository riskLevelHistoryRepository;

  public RiskDetailService(
      RiskStateRepository riskStateRepository,
      RiskSignalRepository riskSignalRepository,
      RiskLevelHistoryRepository riskLevelHistoryRepository) {
    this.riskStateRepository = riskStateRepository;
    this.riskSignalRepository = riskSignalRepository;
    this.riskLevelHistoryRepository = riskLevelHistoryRepository;
  }

  public Mono<RiskDetailResponse> getDetail(Long userId) {
    Mono<RiskState> stateMono = riskStateRepository.findById(userId);
    Mono<List<RiskSignal>> signalMono =
        riskSignalRepository.findRecentByUserId(userId).collectList();
    Mono<List<RiskLevelHistory>> historyMono =
        riskLevelHistoryRepository.findRecentByUserId(userId, 10).collectList();
    return Mono.zip(stateMono, signalMono, historyMono)
        .map(
            tuple -> {
              RiskState state = tuple.getT1();
              List<RiskSignal> signals = tuple.getT2();
              List<RiskLevelHistory> history = tuple.getT3();
              String level = state.getLastLevel();
              RiskSignal worst =
                  signals.stream()
                      .max(Comparator.comparingInt(s -> severityRank(s.getSeverity())))
                      .orElse(null);
              String insightKey;
              String recommendationKey;
              if (worst == null) {
                insightKey = INSIGHT_STABLE;
                recommendationKey = REC_STABLE;
              } else {
                insightKey = mapInsightKey(worst.getSignalType());
                recommendationKey = mapRecommendationKey(worst.getSignalType());
              }
              return RiskDetailResponse.builder()
                  .currentLevel(level)
                  .score(scoreOf(level))
                  .color(colorOf(level))
                  .topInsightKey(insightKey)
                  .recommendationKey(recommendationKey)
                  .lastDetectedAt(worst != null ? worst.getDetectedAt() : null)
                  .activeSignals(
                      signals.stream()
                          .map(
                              signal ->
                                  RiskDetailResponse.ActiveSignalItem.builder()
                                      .signalType(signal.getSignalType())
                                      .severity(signal.getSeverity())
                                      .detectedAt(signal.getDetectedAt())
                                      .build())
                          .toList())
                  .recentLevelChanges(
                      history.stream()
                          .map(
                              lvlHistory ->
                                  RiskDetailResponse.LevelChangeItem.builder()
                                      .oldLevel(lvlHistory.getOldLevel())
                                      .newLevel(lvlHistory.getNewLevel())
                                      .occurredAt(lvlHistory.getOccurredAt())
                                      .build())
                          .toList())
                  .activeSignalSummaries(aggregateSignals(signals))
                  .build();
            });
  }

  private List<RiskSignalSummary> aggregateSignals(List<RiskSignal> signals) {
    return signals.stream()
        .collect(Collectors.groupingBy(RiskSignal::getSignalType))
        .entrySet()
        .stream()
        .map(
            entry -> {
              String signalType = entry.getKey();
              List<RiskSignal> grouped = entry.getValue();
              RiskSignal latest =
                  grouped.stream()
                      .max(Comparator.comparing(RiskSignal::getDetectedAt))
                      .orElse(grouped.get(0));
              return new RiskSignalSummary(
                  signalType, latest.getSeverity(), grouped.size(), latest.getDetectedAt());
            })
        .sorted(Comparator.comparing(RiskSignalSummary::getSeverity).reversed())
        .toList();
  }
}
