package com.alvayonara.finguardriskservice.subscription.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GoogleWebhookService {
  private final ObjectMapper mapper = new ObjectMapper();

  public Mono<GoogleWebhookNotification> parse(String body) {
    return Mono.fromCallable(
        () -> {
          JsonNode root = mapper.readTree(body);
          JsonNode messageNode = root.path("message");
          String messageId = messageNode.path("messageId").asText();
          String publishTimeStr = messageNode.path("publishTime").asText();
          LocalDateTime publishedAt =
              LocalDateTime.ofInstant(Instant.parse(publishTimeStr), ZoneOffset.UTC);
          String base64Data = messageNode.path("data").asText();
          byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
          String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);
          JsonNode dataNode = mapper.readTree(decodedJson);
          JsonNode subscriptionNotification = dataNode.path("subscriptionNotification");
          String purchaseToken = subscriptionNotification.path("purchaseToken").asText();
          return new GoogleWebhookNotification(messageId, purchaseToken, publishedAt, body);
        });
  }
}
