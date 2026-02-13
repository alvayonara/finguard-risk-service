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

  Mono<Category> findByUserIdAndName(Long userId, String name);
}
