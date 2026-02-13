package com.alvayonara.finguardriskservice.transaction;

import com.alvayonara.finguardriskservice.spending.summary.CategorySumProjection;
import com.alvayonara.finguardriskservice.spending.summary.TypeSumProjection;
import com.alvayonara.finguardriskservice.spending.trend.MonthlySumProjection;
import com.alvayonara.finguardriskservice.transaction.dto.RecentTransactionProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
  @Query(
      """
                        SELECT COALESCE(AVG(amount), 0)
                        FROM transactions
                        WHERE user_id = :userId
                          AND type = 'EXPENSE'
                          AND occurred_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                    """)
  Mono<BigDecimal> findAvgExpenseLast30Days(Long userId);

  @Query(
      """
                        SELECT type, SUM(amount) as total
                        FROM transactions
                        WHERE user_id = :userId
                          AND occurred_at >= :start
                          AND occurred_at < :end
                        GROUP BY type
                    """)
  Flux<TypeSumProjection> sumByType(Long userId, LocalDate start, LocalDate end);

  @Query(
      """
                        SELECT category_id, SUM(amount) as total
                        FROM transactions
                        WHERE user_id = :userId
                          AND type = 'EXPENSE'
                          AND occurred_at >= :start
                          AND occurred_at < :end
                        GROUP BY category_id
                    """)
  Flux<CategorySumProjection> sumExpenseByCategory(Long userId, LocalDate start, LocalDate end);

  @Query(
      """
                        SELECT COALESCE(SUM(amount), 0) as total
                        FROM transactions
                        WHERE user_id = :userId
                          AND type = 'EXPENSE'
                          AND occurred_at >= :start
                          AND occurred_at < :end
                    """)
  Mono<BigDecimal> sumExpenseOnly(Long userId, LocalDate start, LocalDate end);

  @Query(
      """
                        SELECT
                            YEAR(occurred_at) as year,
                            MONTH(occurred_at) as month,
                            COALESCE(SUM(amount), 0) as total
                        FROM transactions
                        WHERE user_id = :userId
                          AND type = 'EXPENSE'
                          AND occurred_at >= :start
                          AND occurred_at < :end
                        GROUP BY YEAR(occurred_at), MONTH(occurred_at)
                        ORDER BY YEAR(occurred_at), MONTH(occurred_at)
                    """)
  Flux<MonthlySumProjection> sumExpenseGroupedByMonth(Long userId, LocalDate start, LocalDate end);

  @Query(
      """
            SELECT COALESCE(SUM(amount), 0)
                                      FROM transactions
                                      WHERE user_id = :userId
                                        AND category_id = :categoryId
                                        AND type = 'EXPENSE'
                                        AND occurred_at >= :start
                                        AND occurred_at < :end
            """)
  Mono<BigDecimal> sumExpenseByCategoryIdAndPeriod(
      Long userId, Long categoryId, LocalDate start, LocalDate end);

  @Query(
      """
                SELECT t.id, t.type, t.amount, t.category_id, c.name as category, t.occurred_at
                FROM transactions t
                JOIN categories c ON t.category_id = c.id
                WHERE t.user_id = :userId
                ORDER BY t.occurred_at DESC
                LIMIT 5
            """)
  Flux<RecentTransactionProjection> findRecentWithCategory(Long userId);
}
