package com.alvayonara.finguardriskservice.budget;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BudgetConfigRepository extends ReactiveCrudRepository<BudgetConfig, Long> {
  Mono<BudgetConfig> findByUserIdAndCategory(Long userId, String category);

  Flux<BudgetConfig> findByUserId(Long userId);
}
