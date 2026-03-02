package com.alvayonara.finguardriskservice.subscription.google;

import com.alvayonara.finguardriskservice.subscription.SubscriptionRepository;
import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GooglePlayVerificationService {

  private final WebClient webClient;
  private final SubscriptionRepository subscriptionRepository;
  private final String packageName;
  private final String serviceAccountPath;

  public GooglePlayVerificationService(
      WebClient.Builder builder,
      SubscriptionRepository subscriptionRepository,
      @Value("${android.package}") String packageName,
      @Value("${google.service-account-path}") String serviceAccountPath) {
    this.webClient = builder.baseUrl("https://androidpublisher.googleapis.com").build();
    this.subscriptionRepository = subscriptionRepository;
    this.packageName = packageName;
    this.serviceAccountPath = serviceAccountPath;
  }

  public Mono<SubscriptionValidationResult> verify(String productId, String purchaseToken) {
    return callGoogleApi(productId, purchaseToken);
  }

  public Mono<SubscriptionValidationResult> verifyLatestByToken(String purchaseToken) {
    return subscriptionRepository
        .findByExternalTransactionId(purchaseToken)
        .switchIfEmpty(Mono.error(new RuntimeException("Subscription not found for token")))
        .flatMap(subscription -> callGoogleApi(subscription.getProductId(), purchaseToken));
  }

  private Mono<SubscriptionValidationResult> callGoogleApi(String productId, String purchaseToken) {
    return Mono.fromCallable(
            () -> {
              GoogleCredentials credentials =
                  GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath))
                      .createScoped(
                          Collections.singleton(
                              "https://www.googleapis.com/auth/androidpublisher"));
              credentials.refreshIfExpired();
              return credentials.getAccessToken().getTokenValue();
            })
        .flatMap(
            accessToken ->
                webClient
                    .get()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path(
                                    "/androidpublisher/v3/applications/{packageName}/purchases/subscriptions/{productId}/tokens/{token}")
                                .build(packageName, productId, purchaseToken))
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(
                        json -> {
                          if (!json.has("expiryTimeMillis")) {
                            throw new RuntimeException("Invalid Google subscription response");
                          }
                          long expiryMillis = Long.parseLong(json.get("expiryTimeMillis").asText());
                          LocalDateTime expiry =
                              LocalDateTime.ofEpochSecond(expiryMillis / 1000, 0, ZoneOffset.UTC);

                          /*
                          autoRenewing: true/false
                          */
                          boolean autoRenew =
                              json.has("autoRenewing") && json.get("autoRenewing").asBoolean();

                          /*
                          cancelReason:
                          0 = user canceled
                          1 = system canceled (billing failure)
                          2 = replaced
                          3 = developer canceled
                          */
                          boolean canceled = json.has("cancelReason");

                          /*
                          paymentState:
                          0 = pending
                          1 = received
                          2 = free trial
                          3 = deferred
                          */
                          boolean paymentValid =
                              !json.has("paymentState")
                                  || json.get("paymentState").asInt() == 1
                                  || json.get("paymentState").asInt() == 2;

                          if (!paymentValid) {
                            throw new RuntimeException("Payment not completed");
                          }
                          return new SubscriptionValidationResult(
                              productId, expiry, autoRenew, canceled);
                        }));
  }
}
