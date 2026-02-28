package com.alvayonara.finguardriskservice.subscription.apple;

import com.nimbusds.jose.jwk.JWKSet;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ApplePublicKeyProvider {
  private final WebClient webClient = WebClient.create("https://api.storekit.itunes.apple.com");
  private final AtomicReference<JWKSet> cachedKeys = new AtomicReference<>();

  public Mono<JWKSet> getKeySet() {
    if (cachedKeys.get() != null) {
      return Mono.just(cachedKeys.get());
    }
    return webClient
        .get()
        .uri("/inApps/v1/notifications/publicKeys")
        .retrieve()
        .bodyToMono(String.class)
        .map(
            json -> {
              try {
                JWKSet set = JWKSet.parse(json);
                cachedKeys.set(set);
                return set;
              } catch (ParseException e) {
                throw new RuntimeException("Invalid Apple key set", e);
              }
            });
  }
}
