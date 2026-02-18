package com.alvayonara.finguardriskservice.subscription.google;

import com.alvayonara.finguardriskservice.subscription.SubscriptionEvent;
import com.alvayonara.finguardriskservice.subscription.SubscriptionEventRepository;
import com.alvayonara.finguardriskservice.subscription.SubscriptionPlatform;
import com.alvayonara.finguardriskservice.subscription.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class GoogleWebhookService {
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionEventRepository eventRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public Mono<Void> handle(String rawBody) {
        return Mono.fromCallable(() -> {
            JsonNode root = mapper.readTree(rawBody);
            String messageData = root.path("message").path("data").asText();
            byte[] decoded = Base64.getDecoder().decode(messageData);
            JsonNode payload = mapper.readTree(decoded);
            String eventId = payload.path("eventTimeMillis").asText();
            String purchaseToken = payload.path("subscriptionNotification")
                            .path("purchaseToken")
                            .asText();
            String notificationType = payload.path("subscriptionNotification")
                            .path("notificationType")
                            .asText();
            return new GoogleEvent(eventId, purchaseToken, notificationType, payload);
        }).flatMap(event ->
                eventRepository.save(
                                SubscriptionEvent.builder()
                                        .platform(SubscriptionPlatform.ANDROID.name())
                                        .eventId(event.eventId())
                                        .externalTransactionId(event.purchaseToken())
                                        .type(event.type())
                                        .payload(event.payload().toString())
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        ).onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                        .then(processGoogleEvent(event))
        );
    }

    private Mono<Void> processGoogleEvent(GoogleEvent event) {
        return subscriptionService.syncGoogleSubscription(event.purchaseToken());
    }

    private record GoogleEvent(
            String eventId,
            String purchaseToken,
            String type,
            JsonNode payload) {}
}
