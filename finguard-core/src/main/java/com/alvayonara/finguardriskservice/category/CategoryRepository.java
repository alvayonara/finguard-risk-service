package com.alvayonara.finguardriskservice.category;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveCrudRepository<Category, Long> {
  @Query(
      """
                SELECT *
                FROM categories
                WHERE user_id = :userId
                   OR is_default = TRUE
                ORDER BY is_default DESC, name ASC
            """)
  Flux<Category> findAllByUser(Long userId);

  Mono<Category> findByIdAndUserId(Long id, Long userId);

  @Query(
      """
                SELECT *
                FROM categories
                WHERE (user_id = :userId OR is_default = TRUE)
                  AND name = :name
                  AND type = :type
                LIMIT 1
            """)
  Mono<Category> findByUserOrDefaultAndNameAndType(Long userId, String name, String type);

  @Query(
      """
                SELECT *
                FROM categories
                WHERE name = 'Other'
                  AND type = :type
                  AND is_default = TRUE
                LIMIT 1
            """)
  Mono<Category> findOtherDefaultCategory(String type);

  @Query(
      """
                SELECT id
                FROM transactions
                WHERE category_id = :categoryId
                  AND id > :lastId
                ORDER BY id ASC
                LIMIT :limit
            """)
  Flux<Long> findTransactionIdsByCategoryIdAfter(
      Long categoryId, Long lastId, int limit);

  @Query(
      """
                UPDATE transactions
                SET category_id = :newCategoryId
                WHERE category_id = :oldCategoryId
                  AND id > :lastId
                LIMIT :limit
            """)
  Mono<Integer> updateTransactionCategoriesBatch(
      Long newCategoryId, Long oldCategoryId, Long lastId, int limit);
}
