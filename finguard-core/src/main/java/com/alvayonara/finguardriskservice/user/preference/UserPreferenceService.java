package com.alvayonara.finguardriskservice.user.preference;

import com.alvayonara.finguardriskservice.user.UserRepository;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserPreferenceService {
  @Autowired private UserRepository userRepository;

  public Mono<UserPreferenceResponse> get(Long internalUserId) {
    return userRepository
        .findById(internalUserId)
        .map(
            user ->
                UserPreferenceResponse.builder()
                    .currency(
                        Objects.nonNull(user.getPreferredCurrency())
                            ? user.getPreferredCurrency()
                            : "USD")
                    .language(
                        Objects.nonNull(user.getPreferredLanguage())
                            ? user.getPreferredLanguage()
                            : "en")
                    .build());
  }

  public Mono<Void> update(Long internalUserId, UserPreferenceRequest request) {
    return userRepository
        .findById(internalUserId)
        .flatMap(
            user -> {
              if (Objects.nonNull(request.getCurrency())) {
                user.setPreferredCurrency(request.getCurrency());
              }
              if (Objects.nonNull(request.getLanguage())) {
                user.setPreferredLanguage(request.getLanguage());
              }
              return userRepository.save(user);
            })
        .then();
  }
}
