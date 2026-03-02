package com.alvayonara.finguardriskservice.subscription;

import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionPurchaseRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/subscription")
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  public SubscriptionController(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @PreAuthorize("hasRole('USER')")
  @PostMapping("/purchase")
  public Mono<Void> purchase(
      @AuthenticationPrincipal Jwt jwt, @RequestBody SubscriptionPurchaseRequest request) {
    String userUid = jwt.getSubject();
    return subscriptionService.purchaseSubscription(userUid, request);
  }
}
