package com.alvayonara.finguardriskservice.featureflag;

import com.alvayonara.finguardriskservice.featureflag.dto.AppConfigResponse;
import com.alvayonara.finguardriskservice.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/app-config")
public class AppConfigController {
    @Autowired
    private FeatureFlagService featureFlagService;
    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    public Mono<AppConfigResponse> getConfig(@AuthenticationPrincipal Jwt jwt,
                                             @RequestHeader("X-Platform") String platform,
                                             @RequestHeader("X-App-Version") String version
    ) {
        String userUid = jwt.getSubject();
        return subscriptionService.resolveEffectivePlan(userUid)
                .flatMap(plan -> {
                    FeatureContext context =
                            new FeatureContext(
                                    userUid,
                                    plan,
                                    platform,
                                    version
                            );
                    Mono<Boolean> subscription = featureFlagService.isEnabled(FeatureFlagConstants.SUBSCRIPTION_ENABLED, context);
                    Mono<Boolean> community = featureFlagService.isEnabled(FeatureFlagConstants.COMMUNITY_ENABLED, context);
                    return Mono.zip(subscription, community)
                            .map(tuple -> new AppConfigResponse(tuple.getT1(), tuple.getT2()));
                });
    }
}