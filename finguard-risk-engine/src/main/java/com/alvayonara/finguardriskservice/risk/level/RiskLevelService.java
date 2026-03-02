package com.alvayonara.finguardriskservice.risk.level;

import static com.alvayonara.finguardriskservice.risk.level.RiskLevelConstants.*;

import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RiskLevelService {

  private final RiskSignalRepository riskSignalRepository;

  public RiskLevelService(RiskSignalRepository riskSignalRepository) {
    this.riskSignalRepository = riskSignalRepository;
  }

  public Mono<RiskLevelResponse> getRiskLevel(Long userId) {
    return riskSignalRepository
        .findRecentByUserId(userId)
        .collectList()
        .map(
            signals -> {
              String highestSeverity =
                  signals.stream()
                      .map(RiskSignal::getSeverity)
                      .max(this::compareSeverity)
                      .orElse(LOW);
              return toResponse(highestSeverity);
            });
  }

  private int compareSeverity(String s1, String s2) {
    return severityRank(s1) - severityRank(s2);
  }

  private int severityRank(String severity) {
    return switch (severity) {
      case HIGH -> 3;
      case MEDIUM -> 2;
      default -> 1;
    };
  }

  private RiskLevelResponse toResponse(String severity) {
    return switch (severity) {
      case HIGH -> RiskLevelResponse.builder().level(HIGH).score(SCORE_HIGH).color(RED).build();
      case MEDIUM -> RiskLevelResponse.builder()
          .level(MEDIUM)
          .score(SCORE_MEDIUM)
          .color(ORANGE)
          .build();
      default -> RiskLevelResponse.builder().level(LOW).score(SCORE_LOW).color(GREEN).build();
    };
  }
}
