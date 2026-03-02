package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.apple.AppleVerificationService;
import com.alvayonara.finguardriskservice.subscription.google.GoogleWebhookService;
import com.alvayonara.finguardriskservice.subscription.security.ReplayProtectionService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/webhook")
public class SubscriptionWebhookController {

  private final GoogleWebhookService googleWebhookService;
  private final AppleVerificationService appleVerificationService;
  private final ReplayProtectionService replayProtectionService;
  private final SubscriptionService subscriptionService;

  public SubscriptionWebhookController(
      GoogleWebhookService googleWebhookService,
      AppleVerificationService appleVerificationService,
      ReplayProtectionService replayProtectionService,
      SubscriptionService subscriptionService) {
    this.googleWebhookService = googleWebhookService;
    this.appleVerificationService = appleVerificationService;
    this.replayProtectionService = replayProtectionService;
    this.subscriptionService = subscriptionService;
  }

  @PostMapping("/google")
  public Mono<Void> googleWebhook(@RequestBody String body) {
    return googleWebhookService
        .parse(body)
        .flatMap(
            notification ->
                replayProtectionService
                    .validateFreshness(notification.publishedAt())
                    .then(
                        replayProtectionService.storeEvent(
                            SubscriptionEvent.builder()
                                .platform(SubscriptionPlatform.ANDROID.name())
                                .eventId(notification.eventId())
                                .externalTransactionId(notification.purchaseToken())
                                .type("GOOGLE_RTDN")
                                .payload(notification.rawPayload())
                                .createdAt(LocalDateTime.now())
                                .build()))
                    .then(
                        subscriptionService.syncGoogleSubscription(notification.purchaseToken())));
  }

  @PostMapping("/apple")
  public Mono<Void> appleWebhook(@RequestBody Map<String, String> body) {
    String signedPayload = body.get("signedPayload");
    if (signedPayload == null) {
      return Mono.error(new RuntimeException("Missing signedPayload"));
    }
    return appleVerificationService
        .parseServerNotification(signedPayload)
        .flatMap(
            notification ->
                replayProtectionService
                    .validateFreshness(notification.signedAt())
                    .then(
                        replayProtectionService.storeEvent(
                            SubscriptionEvent.builder()
                                .platform(SubscriptionPlatform.IOS.name())
                                .eventId(notification.eventId())
                                .externalTransactionId(notification.externalTransactionId())
                                .type(notification.type())
                                .jti(notification.jti())
                                .signedAt(notification.signedAt())
                                .payload(notification.rawPayload())
                                .createdAt(LocalDateTime.now())
                                .build()))
                    .then(
                        subscriptionService.syncAppleSubscription(
                            notification.externalTransactionId(),
                            notification.validationResult())));
  }
}
