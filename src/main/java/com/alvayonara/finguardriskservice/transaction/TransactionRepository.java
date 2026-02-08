package com.alvayonara.finguardriskservice.transaction;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
    Flux<Transaction> findByUserIdAndOccurredAtBetween(Long userId, LocalDate start, LocalDate end);
}
