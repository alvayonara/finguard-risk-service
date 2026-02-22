package com.alvayonara.finguardriskservice.user;

import java.time.LocalDateTime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
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
}
