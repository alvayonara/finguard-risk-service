package com.alvayonara.finguardriskservice.risk;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RiskSignalRepository extends ReactiveCrudRepository<RiskSignal, Long> {
    Flux<RiskSignal> findByUserIdOrderByDetectedAtDesc(Long userId);
}
