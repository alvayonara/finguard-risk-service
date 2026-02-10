package com.alvayonara.finguardriskservice.risk.signal;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RiskSignalRepository extends ReactiveCrudRepository<RiskSignal, Long> {
  Flux<RiskSignal> findByUserIdOrderByDetectedAtDesc(Long userId);

  @Query(
      """
                SELECT *
                FROM risk_signal
                WHERE user_id = :userId
                ORDER BY detected_at DESC
                LIMIT 10
            """)
  Flux<RiskSignal> findLatestByUserId(Long userId);

  @Query(
      """
                SELECT *
                FROM risk_signal
                WHERE user_id = :userId
                  AND detected_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            """)
  Flux<RiskSignal> findRecentByUserId(Long userId);
}
