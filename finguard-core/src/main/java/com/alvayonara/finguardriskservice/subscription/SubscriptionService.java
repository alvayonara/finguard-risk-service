package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.apple.AppleVerificationService;
import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionPurchaseRequest;
import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionValidationResult;
import com.alvayonara.finguardriskservice.subscription.google.GooglePlayVerificationService;
import com.alvayonara.finguardriskservice.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
        Mono<SubscriptionValidationResult> validationMono;
        switch (platform) {
            case ANDROID ->
                    validationMono = googlePlayVerificationService.verify(request.productId(), request.transactionData());
            case IOS -> validationMono = appleVerificationService.verifySignedTransaction(request.transactionData());
            default -> {
                return Mono.error(new RuntimeException("Unsupported platform"));
            }
        }
        return validationMono
                .flatMap(result ->
                        upsertSubscription(
                                userUid,
                                platform.name(),
                                result.productId(),
                                request.transactionData(),
                                result.expiry(),
                                result.autoRenew(),
                                result.canceled()
                        )
                )
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public Mono<String> resolveEffectivePlan(String userUid) {
        return subscriptionRepository
                .findFirstByUserUidAndStatusOrderByExpiresAtDesc(userUid, SubscriptionStatus.ACTIVE.name())
                .flatMap(subscription -> {
                    LocalDateTime now = LocalDateTime.now();
                    if (subscription.getExpiresAt().isAfter(now)) {
                        return Mono.just(SubscriptionPlan.PREMIUM.name());
                    }
                    return revalidateSubscription(subscription);
                })
                .switchIfEmpty(updateUserPlan(userUid, SubscriptionPlan.FREE.name()).thenReturn(SubscriptionPlan.FREE.name()));
    }

    private Mono<String> revalidateSubscription(Subscription subscription) {
        Mono<SubscriptionValidationResult> validationMono;
        if (SubscriptionPlatform.ANDROID.name().equals(subscription.getPlatform())) {
            validationMono = googlePlayVerificationService.verifyLatestByToken(subscription.getExternalTransactionId());
        } else {
            validationMono = appleVerificationService.verifyLatestByTransaction(subscription.getExternalTransactionId());
        }
        return validationMono
                .flatMap(result -> {
                    LocalDateTime now = LocalDateTime.now();
                    if (result.expiry().isAfter(now)) {
                        subscription.setExpiresAt(result.expiry());
                        subscription.setAutoRenew(result.autoRenew());
                        subscription.setStatus(
                                result.canceled()
                                        ? SubscriptionStatus.CANCELED.name()
                                        : SubscriptionStatus.ACTIVE.name()
                        );
                        subscription.setUpdatedAt(now);
                        return subscriptionRepository
                                .save(subscription)
                                .thenReturn(SubscriptionPlan.PREMIUM.name());
                    }
                    return expireSubscription(subscription);
                })
                .onErrorResume(e -> expireSubscription(subscription));
    }

    private Mono<Void> upsertSubscription(
            String userUid,
            String platform,
            String productId,
            String externalTransactionId,
            LocalDateTime expiresAt,
            boolean autoRenew,
            boolean canceled
    ) {
        LocalDateTime now = LocalDateTime.now();
        return subscriptionRepository
                .findByExternalTransactionId(externalTransactionId)
                .flatMap(existing -> {
                    existing.setExpiresAt(expiresAt);
                    existing.setAutoRenew(autoRenew);
                    existing.setStatus(
                            canceled
                                    ? SubscriptionStatus.CANCELED.name()
                                    : SubscriptionStatus.ACTIVE.name()
                    );
                    existing.setUpdatedAt(now);
                    return subscriptionRepository.save(existing);
                })
                .switchIfEmpty(
                        subscriptionRepository.save(
                                Subscription.builder()
                                        .userUid(userUid)
                                        .plan(SubscriptionPlan.PREMIUM.name())
                                        .status(
                                                canceled
                                                        ? SubscriptionStatus.CANCELED.name()
                                                        : SubscriptionStatus.ACTIVE.name()
                                        )
                                        .platform(platform)
                                        .productId(productId)
                                        .externalTransactionId(
                                                externalTransactionId
                                        )
                                        .startedAt(now)
                                        .expiresAt(expiresAt)
                                        .autoRenew(autoRenew)
                                        .createdAt(now)
                                        .updatedAt(now)
                                        .build()
                        )
                )
                .then(updateUserPlan(userUid, SubscriptionPlan.PREMIUM.name()));
    }

    private Mono<String> expireSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED.name());
        subscription.setAutoRenew(false);
        subscription.setUpdatedAt(LocalDateTime.now());
        return subscriptionRepository
                .save(subscription)
                .then(updateUserPlan(subscription.getUserUid(), SubscriptionPlan.FREE.name()))
                .thenReturn(SubscriptionPlan.FREE.name());
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

    public Mono<Void> syncGoogleSubscription(String purchaseToken) {
        return googlePlayVerificationService
                .verifyLatestByToken(purchaseToken)
                .flatMap(result ->
                        updateFromStore(
                                purchaseToken,
                                result.productId(),
                                result.expiry(),
                                result.autoRenew(),
                                result.canceled()
                        )
                );
    }

    public Mono<Void> syncAppleSubscription(String externalTransactionId, SubscriptionValidationResult result) {
        return updateFromStore(
                externalTransactionId,
                result.productId(),
                result.expiry(),
                result.autoRenew(),
                result.canceled()
        );
    }

    private Mono<Void> updateFromStore(
            String externalTransactionId,
            String productId,
            LocalDateTime expiresAt,
            boolean autoRenew,
            boolean canceled
    ) {
        return subscriptionRepository
                .findByExternalTransactionId(externalTransactionId)
                .flatMap(subscription -> {
                    subscription.setExpiresAt(expiresAt);
                    subscription.setAutoRenew(autoRenew);
                    subscription.setStatus(
                            canceled
                                    ? SubscriptionStatus.CANCELED.name()
                                    : SubscriptionStatus.ACTIVE.name()
                    );
                    subscription.setUpdatedAt(LocalDateTime.now());
                    return subscriptionRepository.save(subscription);
                })
                .then();
    }
}