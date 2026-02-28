package com.alvayonara.finguardriskservice.appversion;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AppVersionRepository extends ReactiveCrudRepository<AppVersionConfig, Long> {
  Mono<AppVersionConfig> findByPlatform(String platform);
}
