package com.alvayonara.finguardriskservice.risk.state;

import com.alvayonara.finguardriskservice.risk.event.RiskLevelChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class RiskChangeService {
    @Autowired
    private RiskStateRepository riskStateRepository;
    @Autowired
    private RiskStateWriter riskStateWriter;

    public Mono<RiskLevelChangedEvent> checkAndUpdate(Long userId, String newLevel, String topSignal) {
        return riskStateRepository.findById(userId)
                .flatMap(existing -> {
                    String oldLevel = existing.getLastLevel();
                    if (oldLevel.equals(newLevel)) {
                        return Mono.empty();
                    }
                    return riskStateWriter.updateLevel(userId, newLevel)
                            .thenReturn(buildEvent(userId, oldLevel, newLevel, topSignal));
                })
                .switchIfEmpty(
                        riskStateWriter.insertInitial(userId, newLevel).then(Mono.empty())
                );
    }

    private RiskLevelChangedEvent buildEvent(Long userId, String oldLevel, String newLevel, String topSignal) {
        return RiskLevelChangedEvent.builder()
                .userId(userId)
                .oldLevel(oldLevel)
                .newLevel(newLevel)
                .topSignalType(topSignal)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
