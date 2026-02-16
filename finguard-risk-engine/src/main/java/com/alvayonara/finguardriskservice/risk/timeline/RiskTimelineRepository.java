package com.alvayonara.finguardriskservice.risk.timeline;

import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistory;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RiskTimelineRepository extends ReactiveCrudRepository<RiskLevelHistory, Long> {
  @Query(
      """
        SELECT * FROM risk_level_history
        WHERE user_id = :userId
          AND (occurred_at < :cursorTime
               OR (occurred_at = :cursorTime AND id < :cursorId))
        ORDER BY occurred_at DESC, id DESC
        LIMIT :limit
    """)
  Flux<RiskLevelHistory> findNextPage(
      Long userId, LocalDateTime cursorTime, Long cursorId, int limit);

  @Query(
      """
        SELECT * FROM risk_level_history
        WHERE user_id = :userId
        ORDER BY occurred_at DESC, id DESC
        LIMIT :limit
    """)
  Flux<RiskLevelHistory> findFirstPage(Long userId, int limit);
}
