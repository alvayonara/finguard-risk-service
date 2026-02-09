package com.alvayonara.finguardriskservice.risk.level.history;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RiskLevelHistoryRepository extends ReactiveCrudRepository<RiskLevelHistory, Long> {
    Flux<RiskLevelHistory> findByUserIdOrderByOccurredAtDesc(Long userId);
}
