package com.alvayonara.finguardriskservice.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
  Mono<User> findByAnonymousId(String anonymousId);

  Mono<User> findByUserUid(String userUid);

  Mono<User> findByGoogleSub(String googleSub);
}
