package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionValidationResult;
import com.alvayonara.finguardriskservice.subscription.dto.AppleReceiptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
public class AppleVerificationService {
    private final WebClient webClient = WebClient.create();
    @Value("${apple.receipt-url}")
    private String receiptUrl;
    @Value("${apple.shared-secret}")
    private String secret;
    public Mono<SubscriptionValidationResult> verify(String receiptData) {
        Map<String, Object> requestBody = Map.of(
                "receipt-data", receiptData,
                "password", secret
        );
        return webClient.post()
                .uri(receiptUrl)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AppleReceiptResponse.class)
                .map(response -> {
                    if (CollectionUtils.isEmpty(response.getLatestReceiptInfo())) {
                        throw new RuntimeException("Invalid Apple receipt");
                    }
                    AppleReceiptResponse.LatestReceiptInfo latest = response.getLatestReceiptInfo().getLast();
                    LocalDateTime expiry = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(latest.getExpiresDateMs())), ZoneOffset.UTC);
                    boolean canceled = latest.getCancellationDateMs() != null;
                    return new SubscriptionValidationResult(expiry, canceled);
                });
    }
}