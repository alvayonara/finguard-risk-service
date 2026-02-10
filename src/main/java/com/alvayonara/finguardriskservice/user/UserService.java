package com.alvayonara.finguardriskservice.user;

import com.alvayonara.finguardriskservice.common.id.IdGenerator;
import com.alvayonara.finguardriskservice.common.id.IdPrefix;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  @Autowired private UserRepository userRepository;

  public Mono<User> createOrGetAnonymousUser(String anonymousId) {
    return userRepository
        .findByAnonymousId(anonymousId)
        .switchIfEmpty(
            userRepository.save(
                User.builder()
                    .userUid(IdGenerator.generate(IdPrefix.USER))
                    .anonymousId(anonymousId)
                    .createdAt(LocalDateTime.now())
                    .build()));
  }
}
