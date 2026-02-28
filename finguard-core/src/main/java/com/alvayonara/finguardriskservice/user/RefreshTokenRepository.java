package com.alvayonara.finguardriskservice.user;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, Long> {
  Mono<RefreshToken> findByToken(String token);

  @Query(
      """
                        UPDATE refresh_tokens
                        SET revoked = TRUE,
                            revoked_at = :revokedAt
                        WHERE user_uid = :userUid
                          AND revoked = FALSE
                    """)
  Mono<Integer> revokeAllActiveTokensByUserUid(String userUid, LocalDateTime revokedAt);

  @Query(
      """
                SELECT id FROM refresh_tokens
                WHERE revoked = TRUE
                AND expires_at < :threshold
                ORDER BY id
                LIMIT :limit
            """)
  Flux<Long> findExpiredRevokedTokenIds(LocalDateTime threshold, int limit);

  @Query(
      """
                DELETE FROM refresh_tokens
                WHERE id IN (:ids)
            """)
  Mono<Void> deleteByIds(List<Long> ids);
}
