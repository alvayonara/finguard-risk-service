package com.alvayonara.finguardriskservice.user;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RefreshTokenCleanupJob {
  private static final int BATCH_SIZE = 500;
  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Scheduled(cron = "0 0 3 * * ?")
  public void cleanupExpiredTokens() {
    log.info("====== starting refresh token cleanup job ======");
    runBatchDeletion()
        .doOnSuccess(v -> log.info("====== refresh token cleanup completed ======"))
        .doOnError(e -> log.error("====== refresh token cleanup failed ======", e))
        .subscribe();
  }

  private Mono<Void> runBatchDeletion() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(7);
    return refreshTokenRepository
        .findExpiredRevokedTokenIds(threshold, BATCH_SIZE)
        .collectList()
        .flatMap(
            ids -> {
              if (ids.isEmpty()) {
                return Mono.empty();
              }
              return refreshTokenRepository.deleteByIds(ids).then(runBatchDeletion());
            });
  }
}
