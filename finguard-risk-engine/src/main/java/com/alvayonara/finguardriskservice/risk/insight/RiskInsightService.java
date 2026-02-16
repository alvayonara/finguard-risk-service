package com.alvayonara.finguardriskservice.risk.insight;

import static com.alvayonara.finguardriskservice.risk.common.RiskInsightMapper.mapInsightKey;

import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class RiskInsightService {
  @Autowired private RiskSignalRepository riskSignalRepository;

  public Flux<RiskInsightResponse> getInsights(Long userId) {
    return riskSignalRepository.findLatestByUserId(userId).map(this::toInsight);
  }

  private RiskInsightResponse toInsight(RiskSignal signal) {
    String message = mapInsightKey(signal.getSignalType());
    return RiskInsightResponse.builder()
        .type(signal.getSignalType())
        .severity(signal.getSeverity())
        .message(message)
        .detectedAt(signal.getDetectedAt())
        .build();
  }
}
