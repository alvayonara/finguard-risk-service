package com.alvayonara.finguardriskservice.risk.signal;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RiskSignalRepository extends ReactiveCrudRepository<RiskSignal, Long> {

  @Query(
      """
                SELECT *
                FROM risk_signal
                WHERE user_id = :userId
                  AND is_active = true
                ORDER BY detected_at DESC
                LIMIT 10
            """)
  Flux<RiskSignal> findLatestByUserId(Long userId);

  @Query(
      """
                SELECT *
                FROM risk_signal
                WHERE user_id = :userId
                  AND is_active = true
                  AND detected_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                ORDER BY detected_at DESC
            """)
  Flux<RiskSignal> findRecentByUserId(Long userId);

  @Query(
      """
                SELECT *
                FROM risk_signal
                WHERE user_id = :userId
                  AND month_key = :monthKey
                  AND signal_type = :signalType
                ORDER BY detected_at DESC
                LIMIT 1
            """)
  Mono<RiskSignal> findByUserIdAndMonthKeyAndSignalType(
      Long userId, String monthKey, String signalType);

  @Query(
      """
                UPDATE risk_signal
                SET is_active = false, updated_at = NOW()
                WHERE user_id = :userId
                  AND month_key = :monthKey
                  AND is_active = true
            """)
  Mono<Void> deactivateSignalsForMonth(Long userId, String monthKey);

  @Query(
      """
                UPDATE risk_signal
                SET is_active = false, updated_at = NOW()
                WHERE user_id = :userId
                  AND is_active = true
            """)
  Mono<Void> deactivateAllByUserId(Long userId);

  @Query(
      """
                SELECT *
                FROM risk_signal
                WHERE user_id = :userId
                ORDER BY detected_at DESC
                LIMIT 1
            """)
  Mono<RiskSignal> findMostRecentByUserId(Long userId);
}
