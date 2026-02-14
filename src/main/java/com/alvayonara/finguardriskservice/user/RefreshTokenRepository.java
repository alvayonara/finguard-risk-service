package com.alvayonara.finguardriskservice.user;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, Long> {
  Mono<RefreshToken> findByToken(String token);

  @Query("DELETE FROM refresh_tokens WHERE user_uid = :userUid")
  Mono<Void> deleteAllByUserUid(String userUid);

  @Query("UPDATE refresh_tokens SET revoked = true, revoked_at = NOW() WHERE token = :token")
  Mono<Void> revokeToken(String token);
}
