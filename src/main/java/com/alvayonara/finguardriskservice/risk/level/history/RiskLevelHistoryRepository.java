package com.alvayonara.finguardriskservice.risk.level.history;

import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RiskLevelHistoryRepository extends ReactiveCrudRepository<RiskLevelHistory, Long> {
  @Query(
      """
                SELECT * FROM risk_level_history
                WHERE user_id = :userId
                ORDER BY occurred_at DESC
                LIMIT :limit
            """)
  Flux<RiskLevelHistory> findRecentByUserId(Long userId, int limit);

  @Query(
      """
                SELECT * FROM risk_level_history
                WHERE user_id = :userId
                ORDER BY occurred_at DESC, id DESC
                LIMIT :limit
            """)
  Flux<RiskLevelHistory> findFirstPageByUserId(Long userId, int limit);

  @Query(
      """
                SELECT * FROM risk_level_history
                WHERE user_id = :userId
                  AND (occurred_at < :cursorTime
                       OR (occurred_at = :cursorTime AND id < :cursorId))
                ORDER BY occurred_at DESC, id DESC
                LIMIT :limit
            """)
  Flux<RiskLevelHistory> findNextPageByUserId(
      Long userId, LocalDateTime cursorTime, Long cursorId, int limit);
}
