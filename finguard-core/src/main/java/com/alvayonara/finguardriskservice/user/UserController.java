package com.alvayonara.finguardriskservice.user;

import com.alvayonara.finguardriskservice.user.dto.AuthResponse;
import com.alvayonara.finguardriskservice.user.dto.GoogleLoginRequest;
import com.alvayonara.finguardriskservice.user.dto.RefreshTokenRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/users")
public class UserController {
  @Autowired private UserService userService;

  @PostMapping("/google")
  public Mono<AuthResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
    return userService.loginWithGoogle(request.idToken());
  }

  @PostMapping("/refresh")
  public Mono<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
    return userService.refreshAccessToken(request.refreshToken());
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('USER')")
  public Mono<User> me(@AuthenticationPrincipal Jwt jwt) {
    String userUid = jwt.getSubject();
    return userService.getUserByUid(userUid);
  }

  @PostMapping("/onboarding/complete")
  @PreAuthorize("hasRole('USER')")
  public Mono<Void> completeOnboarding(@AuthenticationPrincipal Jwt jwt) {
    String userUid = jwt.getSubject();
    return userService.completeOnboarding(userUid);
  }

  @PostMapping("/logout")
  @PreAuthorize("hasRole('USER')")
  public Mono<Void> logout(
      @AuthenticationPrincipal Jwt jwt, @RequestBody RefreshTokenRequest request) {
    String userUid = jwt.getSubject();
    return userService.logout(userUid, request.refreshToken());
  }
}
