package com.alvayonara.finguardriskservice.subscription.security;

import com.alvayonara.finguardriskservice.subscription.SubscriptionEvent;
import com.alvayonara.finguardriskservice.subscription.SubscriptionEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class ReplayProtectionService {
    @Autowired
    private SubscriptionEventRepository subscriptionEventRepository;
    @Value("${subscription.max-skew}")
    private long maxSkew;

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
        return subscriptionEventRepository.save(event)
                .onErrorResume(DuplicateKeyException.class,
                        e -> Mono.error(new RuntimeException("Replay detected: duplicate event")))
                .then();
    }
}