package com.alvayonara.finguardriskservice.featureflag;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FeatureFlagRepository extends ReactiveCrudRepository<FeatureFlag, Long> {
    Mono<FeatureFlag> findByFlagKey(String flagKey);
    Flux<FeatureFlag> findAll();
}
