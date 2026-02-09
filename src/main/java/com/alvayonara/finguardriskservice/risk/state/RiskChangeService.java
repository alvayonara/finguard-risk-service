package com.alvayonara.finguardriskservice.risk.state;

import com.alvayonara.finguardriskservice.risk.event.RiskLevelChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class RiskChangeService {
    @Autowired
    private RiskStateRepository riskStateRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    public Mono<RiskLevelChangedEvent> checkAndUpdate(Long userId, String newLevel, String topSignal) {
        return riskStateRepository.findById(userId)
                .flatMap(existing -> {
                    String oldLevel = existing.getLastLevel();
                    if (oldLevel.equals(newLevel)) {
                        return Mono.empty();
                    }
                    existing.setLastLevel(newLevel);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return riskStateRepository.save(existing)
                            .thenReturn(buildEvent(userId, oldLevel, newLevel, topSignal));
                })
                .switchIfEmpty(
                        r2dbcEntityTemplate.insert(RiskState.class)
                                .using(buildNewState(userId, newLevel))
                                .then(Mono.empty())
                );
    }

    private RiskState buildNewState(Long userId, String level) {
        RiskState state = new RiskState();
        state.setUserId(userId);
        state.setLastLevel(level);
        state.setUpdatedAt(LocalDateTime.now());
        return state;
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
