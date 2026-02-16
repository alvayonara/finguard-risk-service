package com.alvayonara.finguardriskservice.summary;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MonthlySummaryRepository extends ReactiveCrudRepository<MonthlySummary, Long> {
  @Query(
      """
                SELECT * FROM monthly_summary
                WHERE user_id = :userId AND month_key = :monthKey
            """)
  Mono<MonthlySummary> findByUserIdAndMonthKey(Long userId, String monthKey);

  @Query(
      """
                SELECT *
                FROM monthly_summary
                WHERE user_id = :userId
                ORDER BY month_key DESC
                LIMIT 1
            """)
  Mono<MonthlySummary> findLatestByUserId(Long userId);
}
