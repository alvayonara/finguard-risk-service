package com.alvayonara.finguardriskservice.subscription;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SubscriptionRepository extends ReactiveCrudRepository<Subscription, Long> {
  Mono<Subscription> findFirstByUserUidAndStatusOrderByExpiresAtDesc(String userUid, String status);

  Mono<Subscription> findByExternalTransactionId(String externalTransactionId);
}
