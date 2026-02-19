package com.alvayonara.finguardriskservice.budget;

import java.time.LocalDateTime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BudgetConfigRepository extends ReactiveCrudRepository<BudgetConfig, Long> {
    Mono<BudgetConfig> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query(
            """
                        SELECT * FROM budget_config
                        WHERE user_id = :userId
                        ORDER BY created_at DESC, id DESC
                        LIMIT :limit
                    """)
    Flux<BudgetConfig> findFirstPageByUserId(Long userId, int limit);

    @Query(
            """
                        SELECT * FROM budget_config
                        WHERE user_id = :userId
                          AND (created_at < :cursorTime
                               OR (created_at = :cursorTime AND id < :cursorId))
                        ORDER BY created_at DESC, id DESC
                        LIMIT :limit
                    """)
    Flux<BudgetConfig> findNextPageByUserId(
            Long userId, LocalDateTime cursorTime, Long cursorId, int limit);
}
