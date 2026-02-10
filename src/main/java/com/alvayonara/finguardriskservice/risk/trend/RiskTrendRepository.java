package com.alvayonara.finguardriskservice.risk.trend;

import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistory;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RiskTrendRepository extends ReactiveCrudRepository<RiskLevelHistory, Long> {
  @Query(
      """
            SELECT * FROM risk_level_history
            WHERE user_id = :userId
            AND occurred_at >= :since
            ORDER BY occurred_at DESC
            """)
  Flux<RiskLevelHistory> findSince(Long userId, LocalDateTime since);
}
