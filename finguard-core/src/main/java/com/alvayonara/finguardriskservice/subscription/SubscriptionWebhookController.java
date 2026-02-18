package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.apple.AppleVerificationService;
import com.alvayonara.finguardriskservice.subscription.google.GoogleWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/v1/webhook")
public class SubscriptionWebhookController {
    @Autowired
    private GoogleWebhookService googleWebhookService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private AppleVerificationService appleVerificationService;
    @Autowired
    private SubscriptionEventRepository eventRepository;

    @PostMapping("/google")
    public Mono<Void> googleWebhook(@RequestBody String body) {
        return googleWebhookService.handle(body).then();
    }

    @PostMapping("/apple")
    public Mono<Void> appleWebhook(@RequestBody String signedPayload) {
        return appleVerificationService
                .parseServerNotification(signedPayload)
                .flatMap(notification ->
                        eventRepository
                                .findByEventId(notification.eventId())
                                .flatMap(existing -> Mono.empty())
                                .switchIfEmpty(
                                        eventRepository.save(
                                                        SubscriptionEvent.builder()
                                                                .platform(SubscriptionPlatform.IOS.name())
                                                                .eventId(notification.eventId())
                                                                .externalTransactionId(notification.externalTransactionId())
                                                                .type(notification.type())
                                                                .payload(signedPayload)
                                                                .createdAt(LocalDateTime.now())
                                                                .build()
                                                )
                                                .then(
                                                        subscriptionService.syncAppleSubscription(
                                                                notification.externalTransactionId(),
                                                                notification.validationResult()
                                                        )
                                                )
                                )
                ).then();
    }
}
