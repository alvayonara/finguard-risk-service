package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionPurchaseRequest;
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
    @Autowired
    private AppleVerificationService appleVerificationService;
    @Autowired
    private GooglePlayVerificationService googlePlayVerificationService;

    public Mono<Void> purchaseSubscription(String userUid, SubscriptionPurchaseRequest request) {
        SubscriptionPlatform platform = SubscriptionPlatform.valueOf(request.platform());
        Mono<LocalDateTime> expiryMono;
        switch (platform) {
            case ANDROID ->
                    expiryMono = googlePlayVerificationService.verify(request.productId(), request.purchaseToken());
            case IOS -> expiryMono = appleVerificationService.verify(request.purchaseToken());
            default -> {
                return Mono.error(new RuntimeException("Unsupported platform"));
            }
        }
        return expiryMono.flatMap(expiresAt ->
                subscriptionRepository
                        .findByExternalTransactionId(request.purchaseToken())
                        .flatMap(existing -> Mono.empty())
                        .switchIfEmpty(
                                upsertSubscription(
                                        userUid,
                                        platform.name(),
                                        request.productId(),
                                        request.purchaseToken(),
                                        expiresAt
                                )
                        )
                        .then()
        );
    }

    private Mono<Void> upsertSubscription(
            String userUid,
            String platform,
            String productId,
            String externalTransactionId,
            LocalDateTime expiresAt
    ) {
        LocalDateTime now = LocalDateTime.now();
        return subscriptionRepository
                .findFirstByUserUidAndStatusOrderByExpiresAtDesc(
                        userUid,
                        SubscriptionStatus.ACTIVE.name()
                )
                .flatMap(existing -> {
                    existing.setExpiresAt(expiresAt);
                    return subscriptionRepository.save(existing);
                })
                .switchIfEmpty(
                        subscriptionRepository.save(
                                Subscription.builder()
                                        .userUid(userUid)
                                        .plan(SubscriptionPlan.PREMIUM.name())
                                        .status(SubscriptionStatus.ACTIVE.name())
                                        .platform(platform)
                                        .productId(productId)
                                        .externalTransactionId(externalTransactionId)
                                        .startedAt(now)
                                        .expiresAt(expiresAt)
                                        .createdAt(now)
                                        .build()
                        )
                )
                .then(updateUserPlan(userUid, SubscriptionPlan.PREMIUM.name()));
    }

    public Mono<String> resolveEffectivePlan(String userUid) {
        return subscriptionRepository
                .findFirstByUserUidAndStatusOrderByExpiresAtDesc(
                        userUid,
                        SubscriptionStatus.ACTIVE.name()
                )
                .flatMap(subscription -> {
                    if (subscription.getExpiresAt().isBefore(LocalDateTime.now())) {
                        subscription.setStatus(SubscriptionStatus.EXPIRED.name());
                        return subscriptionRepository
                                .save(subscription)
                                .then(updateUserPlan(userUid, SubscriptionPlan.FREE.name()))
                                .thenReturn(SubscriptionPlan.FREE.name());
                    }
                    return Mono.just(subscription.getPlan());
                })
                .switchIfEmpty(
                        updateUserPlan(userUid, SubscriptionPlan.FREE.name())
                                .thenReturn(SubscriptionPlan.FREE.name())
                );
    }

    private Mono<Void> updateUserPlan(String userUid, String plan) {
        return userRepository
                .findByUserUid(userUid)
                .flatMap(user -> {
                    if (!plan.equals(user.getPlan())) {
                        user.setPlan(plan);
                        return userRepository.save(user);
                    }
                    return Mono.just(user);
                })
                .then();
    }
}