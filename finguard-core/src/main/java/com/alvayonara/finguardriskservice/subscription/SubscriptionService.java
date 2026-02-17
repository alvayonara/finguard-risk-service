package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserRepository userRepository;

    public Mono<Void> upgradeToPremium(String userUid, int durationDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(durationDays);
        Subscription subscription =
                Subscription.builder()
                        .userUid(userUid)
                        .plan(SubscriptionPlan.PREMIUM.name())
                        .status(SubscriptionStatus.ACTIVE.name())
                        .startedAt(now)
                        .expiresAt(expiry)
                        .createdAt(now)
                        .build();
        return subscriptionRepository
                .save(subscription)
                .then(updateUserPlan(userUid, SubscriptionPlan.PREMIUM.name()));
    }

    public Mono<String> resolveEffectivePlan(String userUid) {
        return subscriptionRepository
                .findFirstByUserUidAndStatusOrderByExpiresAtDesc(
                        userUid,
                        SubscriptionStatus.ACTIVE.name())
                .flatMap(subscription -> {
                    if (subscription.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return expireSubscription(subscription);
                    }
                    return Mono.just(subscription.getPlan());
                })
                .switchIfEmpty(
                        updateUserPlan(userUid, SubscriptionPlan.FREE.name())
                                .thenReturn(SubscriptionPlan.FREE.name())
                );
    }

    private Mono<String> expireSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED.name());
        return subscriptionRepository
                .save(subscription)
                .then(updateUserPlan(subscription.getUserUid(), SubscriptionPlan.FREE.name()))
                .thenReturn(SubscriptionPlan.FREE.name());
    }

    private Mono<Void> updateUserPlan(String userUid, String plan) {
        return userRepository
                .findByUserUid(userUid)
                .flatMap(user -> {
                    user.setPlan(plan);
                    return userRepository.save(user);
                })
                .then();
    }
}