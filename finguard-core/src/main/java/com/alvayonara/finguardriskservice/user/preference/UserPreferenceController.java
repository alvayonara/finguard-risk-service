package com.alvayonara.finguardriskservice.user.preference;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/user/preferences")
public class UserPreferenceController {

  private final UserPreferenceService service;

  public UserPreferenceController(UserPreferenceService service) {
    this.service = service;
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping
  public Mono<UserPreferenceResponse> get() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return service.get(userContext.getInternalUserId());
        });
  }

  @PreAuthorize("hasRole('USER')")
  @PutMapping
  public Mono<Void> update(@RequestBody UserPreferenceRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return service.update(userContext.getInternalUserId(), request);
        });
  }
}
