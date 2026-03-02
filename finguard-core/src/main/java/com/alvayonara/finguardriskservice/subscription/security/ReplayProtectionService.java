package com.alvayonara.finguardriskservice.subscription.security;

import com.alvayonara.finguardriskservice.subscription.SubscriptionEvent;
import com.alvayonara.finguardriskservice.subscription.SubscriptionEventRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReplayProtectionService {

  private final SubscriptionEventRepository subscriptionEventRepository;
  private final long maxSkew;

  public ReplayProtectionService(
      SubscriptionEventRepository subscriptionEventRepository,
      @Value("${subscription.max-skew}") long maxSkew) {
    this.subscriptionEventRepository = subscriptionEventRepository;
    this.maxSkew = maxSkew;
  }

  public Mono<Void> validateFreshness(LocalDateTime signedAt) {
    if (signedAt == null) {
      return Mono.error(new RuntimeException("Missing signedAt"));
    }
    Instant now = Instant.now();
    Instant signedInstant = signedAt.toInstant(ZoneOffset.UTC);
    if (signedInstant.isBefore(now.minusSeconds(maxSkew))) {
      return Mono.error(new RuntimeException("Webhook too old — possible replay"));
    }
    return Mono.empty();
  }

  public Mono<Void> storeEvent(SubscriptionEvent event) {
    return subscriptionEventRepository
        .save(event)
        .onErrorResume(
            DuplicateKeyException.class,
            e -> Mono.error(new RuntimeException("Replay detected: duplicate event")))
        .then();
  }
}
