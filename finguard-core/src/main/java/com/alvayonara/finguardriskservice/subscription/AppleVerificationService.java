package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.dto.AppleReceiptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class AppleVerificationService {
    private final WebClient webClient = WebClient.create();
    @Value("${apple.receipt-url}")
    private String receiptUrl;
    @Value("${apple.shared-secret}")
    private String secret;

    public Mono<LocalDateTime> verify(String receiptData) {
        Map<String, Object> requestBody = Map.of(
                "receipt-data", receiptData,
                "password", secret
        );
        return webClient.post()
                .uri(receiptUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AppleReceiptResponse.class)
                .map(response -> {
                    if (response.getStatus() != 0) {
                        throw new RuntimeException("Invalid Apple receipt");
                    }
                    String expiry = response.getLatestReceiptInfoList().getFirst().getExpiresDateMs();
                    return LocalDateTime.parse(
                            expiry,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV")
                    );
                });
    }
}
