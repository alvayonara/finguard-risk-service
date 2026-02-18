package com.alvayonara.finguardriskservice.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@Service
public class GooglePlayVerificationService {
    private final WebClient webClient;
    @Value("${android.package}")
    private String packageName;
    @Value("${google.service-account-path}")
    private String serviceAccountPath;

    public GooglePlayVerificationService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://androidpublisher.googleapis.com").build();
    }

    public Mono<LocalDateTime> verify(String productId, String purchaseToken) {
        return Mono.fromCallable(() -> {
            GoogleCredentials credentials =
                    GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath))
                            .createScoped(Collections.singleton(
                                    "https://www.googleapis.com/auth/androidpublisher"
                            ));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        }).flatMap(accessToken ->
                webClient.get()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .path("/androidpublisher/v3/applications/{packageName}/purchases/subscriptions/{productId}/tokens/{token}")
                                        .build(packageName, productId, purchaseToken)
                        )
                        .headers(headers ->
                                headers.setBearerAuth(accessToken)
                        )
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(json -> {
                            long expiryMillis = json.get("expiryTimeMillis").asLong();
                            return LocalDateTime.ofEpochSecond(
                                    expiryMillis / 1000,
                                    0,
                                    ZoneOffset.UTC
                            );
                        })
        );
    }
}