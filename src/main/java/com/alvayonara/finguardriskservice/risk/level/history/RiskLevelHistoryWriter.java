package com.alvayonara.finguardriskservice.risk.level.history;

import com.alvayonara.finguardriskservice.risk.event.RiskLevelChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RiskLevelHistoryWriter {
  @Autowired private RiskLevelHistoryRepository riskLevelHistoryRepository;

  public Mono<Void> insert(RiskLevelChangedEvent event) {
    RiskLevelHistory riskLevelHistory = new RiskLevelHistory();
    riskLevelHistory.setUserId(event.getUserId());
    riskLevelHistory.setOldLevel(event.getOldLevel());
    riskLevelHistory.setNewLevel(event.getNewLevel());
    riskLevelHistory.setTopSignalType(event.getTopSignalType());
    riskLevelHistory.setOccurredAt(event.getOccurredAt());
    return riskLevelHistoryRepository.save(riskLevelHistory).then();
  }
}
