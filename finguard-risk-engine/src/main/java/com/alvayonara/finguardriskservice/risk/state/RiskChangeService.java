package com.alvayonara.finguardriskservice.risk.state;

import com.alvayonara.finguardriskservice.risk.event.RiskLevelChangedEvent;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RiskChangeService {
  @Autowired private RiskStateRepository riskStateRepository;
  @Autowired private RiskStateWriter riskStateWriter;
  @Autowired private RiskSignalRepository riskSignalRepository;

  public Mono<RiskLevelChangedEvent> checkAndUpdate(
      Long userId, String newLevel, String topSignal) {
    return riskStateRepository
        .findById(userId)
        .flatMap(
            existing -> {
              if (existing.getLastLevel().equals(newLevel)) {
                return Mono.empty();
              }
              return riskStateWriter
                  .updateLevel(userId, newLevel)
                  .thenReturn(buildEvent(userId, existing.getLastLevel(), newLevel, topSignal));
            })
        .switchIfEmpty(riskStateWriter.insertInitial(userId, newLevel).then(Mono.empty()));
  }

  public Mono<Void> recalculateUser(Long userId) {
    return riskSignalRepository
        .deactivateAllByUserId(userId)
        .then(riskStateWriter.resetUser(userId));
  }

  private RiskLevelChangedEvent buildEvent(
      Long userId, String oldLevel, String newLevel, String topSignal) {
    return RiskLevelChangedEvent.builder()
        .userId(userId)
        .oldLevel(oldLevel)
        .newLevel(newLevel)
        .topSignalType(topSignal)
        .occurredAt(LocalDateTime.now())
        .build();
  }
}
