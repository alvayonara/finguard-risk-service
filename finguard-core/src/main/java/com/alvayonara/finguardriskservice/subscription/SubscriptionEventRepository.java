package com.alvayonara.finguardriskservice.subscription;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SubscriptionEventRepository extends ReactiveCrudRepository<SubscriptionEvent, Long> {
    Mono<SubscriptionEvent> findByEventId(String eventId);
}