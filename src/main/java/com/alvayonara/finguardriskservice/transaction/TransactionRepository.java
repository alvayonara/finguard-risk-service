package com.alvayonara.finguardriskservice.transaction;


import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
    @Query("""
                SELECT COALESCE(AVG(amount), 0)
                FROM transactions
                WHERE user_id = :userId
                  AND type = 'EXPENSE'
                  AND occurred_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            """)
    Mono<BigDecimal> findAvgExpenseLast30Days(Long userId);

    @Query("""
                SELECT *
                FROM transactions
                WHERE user_id = :userId
                ORDER BY occurred_at DESC
                LIMIT 5
            """)
    Flux<Transaction> findRecentByUserId(Long userId);
}
