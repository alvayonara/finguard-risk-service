package com.alvayonara.finguardriskservice.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/subscription")
public class SubscriptionController {
    @Autowired private SubscriptionService subscriptionService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/upgrade")
    public Mono<Void> upgrade(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "30") int days) {
        String userUid = jwt.getSubject();
        return subscriptionService.upgradeToPremium(userUid, days);
    }
}
