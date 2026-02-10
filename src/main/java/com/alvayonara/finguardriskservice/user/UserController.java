package com.alvayonara.finguardriskservice.user;

import com.alvayonara.finguardriskservice.user.dto.AnonymousUserRequest;
import com.alvayonara.finguardriskservice.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/users")
public class UserController {
  @Autowired private UserService userService;

  @PostMapping("/anonymous")
  public Mono<UserResponse> createAnonymousUser(@RequestBody @Valid AnonymousUserRequest request) {
    return userService
        .createOrGetAnonymousUser(request.anonymousId())
        .map(user -> new UserResponse(user.getId()));
  }
}
