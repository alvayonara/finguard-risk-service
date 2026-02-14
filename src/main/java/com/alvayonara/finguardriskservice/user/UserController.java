package com.alvayonara.finguardriskservice.user;

import com.alvayonara.finguardriskservice.user.dto.AnonymousUserRequest;
import com.alvayonara.finguardriskservice.user.dto.GoogleLoginRequest;
import com.alvayonara.finguardriskservice.user.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/users")
public class UserController {
  @Autowired
  private UserService userService;

  @PostMapping("/anonymous")
  public Mono<AuthResponse> createAnonymousUser(@RequestBody @Valid AnonymousUserRequest request) {
    return userService.createOrGetAnonymousUser(request.anonymousId());
  }

  @PostMapping("/google")
  public Mono<AuthResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
    return userService.loginWithGoogle(request.idToken(), request.anonymousId());
  }
}