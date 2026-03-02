package com.alvayonara.finguardriskservice.risk.trend;

import static com.alvayonara.finguardriskservice.risk.level.RiskLevelConstants.*;

import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RiskTrendService {

  private final RiskTrendRepository riskTrendRepository;

  public RiskTrendService(RiskTrendRepository riskTrendRepository) {
    this.riskTrendRepository = riskTrendRepository;
  }

  public Mono<RiskTrendResponse> getTrend(Long userId, int days) {
    LocalDateTime since = LocalDateTime.now().minusDays(days);
    return riskTrendRepository
        .findSince(userId, since)
        .collectList()
        .map(
            list -> {
              Map<LocalDate, RiskLevelHistory> latestPerDay = new HashMap<>();
              for (RiskLevelHistory riskLevelHistory : list) {
                LocalDate date = riskLevelHistory.getOccurredAt().toLocalDate();
                latestPerDay.putIfAbsent(date, riskLevelHistory);
              }

              List<RiskTrendPoint> points =
                  latestPerDay.values().stream()
                      .sorted(Comparator.comparing(RiskLevelHistory::getOccurredAt))
                      .map(
                          riskLevelHistory ->
                              RiskTrendPoint.builder()
                                  .date(riskLevelHistory.getOccurredAt().toLocalDate().toString())
                                  .level(riskLevelHistory.getNewLevel())
                                  .score(scoreOf(riskLevelHistory.getNewLevel()))
                                  .build())
                      .toList();

              return RiskTrendResponse.builder().points(points).build();
            });
  }

  private int scoreOf(String s) {
    return switch (s) {
      case HIGH -> SCORE_HIGH;
      case MEDIUM -> SCORE_MEDIUM;
      default -> SCORE_LOW;
    };
  }
}
