package com.alvayonara.finguardriskservice.subscription;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SubscriptionEventRepository extends ReactiveCrudRepository<SubscriptionEvent, Long> {
}