package com.alvayonara.finguardriskservice.featureflag;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FeatureFlagService {
  @Autowired private FeatureFlagRepository repository;
  private final Map<String, FeatureFlag> cache = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    refreshCache();
  }

  public void refreshCache() {
    repository
        .findAll()
        .collectList()
        .subscribe(
            flags -> {
              cache.clear();
              flags.forEach(flag -> cache.put(flag.getFlagKey(), flag));
            });
  }

  public Mono<Boolean> isEnabled(String key, FeatureContext context) {
    FeatureFlag flag = cache.get(key);
    if (Objects.isNull(flag)) {
      return Mono.just(false);
    }
    return Mono.just(evaluate(flag, context));
  }

  private boolean evaluate(FeatureFlag flag, FeatureContext ctx) {
    if (!Boolean.TRUE.equals(flag.getEnabled())) {
      return false;
    }
    if (!planMatch(flag, ctx)) {
      return false;
    }
    if (!platformMatch(flag, ctx)) {
      return false;
    }
    if (!versionMatch(flag, ctx)) {
      return false;
    }
    if (!rolloutMatch(flag, ctx.userUid())) {
      return false;
    }
    return true;
  }

  private boolean planMatch(FeatureFlag flag, FeatureContext ctx) {
    if (Objects.isNull(flag.getRequiredPlan())) {
      return true;
    }
    return flag.getRequiredPlan().equalsIgnoreCase(ctx.userPlan());
  }

  private boolean platformMatch(FeatureFlag flag, FeatureContext ctx) {
    if (Objects.isNull(flag.getPlatform())) {
      return true;
    }
    return flag.getPlatform().equalsIgnoreCase(ctx.platform());
  }

  private boolean versionMatch(FeatureFlag flag, FeatureContext ctx) {
    if (Objects.isNull(flag.getMinAppVersion())) {
      return true;
    }
    return compareVersion(ctx.appVersion(), flag.getMinAppVersion()) >= 0;
  }

  private boolean rolloutMatch(FeatureFlag flag, String userUid) {
    int rollout = flag.getRolloutPercentage();
    if (rollout >= 100) {
      return true;
    }
    if (rollout <= 0) {
      return false;
    }
    int bucket = hashToBucket(userUid);
    return bucket < rollout;
  }

  private int hashToBucket(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      int value = ((hash[0] & 0xFF) << 8) | (hash[1] & 0xFF);
      return Math.abs(value) % 100;
    } catch (Exception e) {
      return 0;
    }
  }

  private int compareVersion(String v1, String v2) {
    String[] a = v1.split("\\.");
    String[] b = v2.split("\\.");
    for (int i = 0; i < Math.max(a.length, b.length); i++) {
      int x = i < a.length ? Integer.parseInt(a[i]) : 0;
      int y = i < b.length ? Integer.parseInt(b[i]) : 0;
      if (x != y) {
        return Integer.compare(x, y);
      }
    }
    return 0;
  }
}
