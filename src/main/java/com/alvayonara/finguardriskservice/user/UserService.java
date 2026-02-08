package com.alvayonara.finguardriskservice.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Mono<User> createOrGetAnonymousUser(String anonymousId) {
        return userRepository.findByAnonymousId(anonymousId)
                .switchIfEmpty(userRepository.save(buildAnonymousUser(anonymousId)));
    }

    private User buildAnonymousUser(String anonymousId) {
        User user = new User();
        user.setAnonymousId(anonymousId);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
