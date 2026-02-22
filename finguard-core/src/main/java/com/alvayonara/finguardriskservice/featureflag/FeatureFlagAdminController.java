package com.alvayonara.finguardriskservice.featureflag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/admin/feature-flags")
public class FeatureFlagAdminController {
    @Autowired
    private FeatureFlagRepository featureFlagRepository;
    @Autowired
    private FeatureFlagService featureFlagService;

    @PutMapping("/{key}")
    public Mono<Void> updateFlag(@PathVariable String key, @RequestBody FeatureFlag updated) {
        return featureFlagRepository.findByFlagKey(key)
                .flatMap(existing -> {
                    existing.setEnabled(updated.getEnabled());
                    existing.setRolloutPercentage(updated.getRolloutPercentage());
                    existing.setRequiredPlan(updated.getRequiredPlan());
                    existing.setPlatform(updated.getPlatform());
                    existing.setMinAppVersion(updated.getMinAppVersion());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    return featureFlagRepository.save(existing);
                })
                .doOnSuccess(flag -> featureFlagService.refreshCache())
                .then();
    }
}
