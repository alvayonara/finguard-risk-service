package com.alvayonara.finguardriskservice.user;

import com.alvayonara.finguardriskservice.common.id.IdGenerator;
import com.alvayonara.finguardriskservice.common.id.IdPrefix;
import com.alvayonara.finguardriskservice.security.GoogleAuthService;
import com.alvayonara.finguardriskservice.security.JwtUtil;
import com.alvayonara.finguardriskservice.subscription.SubscriptionService;
import com.alvayonara.finguardriskservice.user.dto.AuthResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final GoogleAuthService googleAuthService;
  private final JwtUtil jwtUtil;
  private final SubscriptionService subscriptionService;

  public UserService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      GoogleAuthService googleAuthService,
      JwtUtil jwtUtil,
      SubscriptionService subscriptionService) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.googleAuthService = googleAuthService;
    this.jwtUtil = jwtUtil;
    this.subscriptionService = subscriptionService;
  }

  public Mono<AuthResponse> loginWithGoogle(String idToken) {
    GoogleIdToken.Payload payload;
    try {
      payload = googleAuthService.verify(idToken);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("Invalid Google token"));
    }
    String googleSub = payload.getSubject();
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    return userRepository
        .findByGoogleSub(googleSub)
        .switchIfEmpty(
            userRepository.save(
                User.builder()
                    .userUid(IdGenerator.generate(IdPrefix.USER))
                    .googleSub(googleSub)
                    .email(email)
                    .name(name)
                    .plan(UserPlan.FREE.name())
                    .createdAt(LocalDateTime.now())
                    .build()))
        .flatMap(
            user ->
                refreshTokenRepository
                    .revokeAllActiveTokensByUserUid(user.getUserUid(), LocalDateTime.now())
                    .then(generateAuthResponse(user, List.of(UserRole.USER.name()))));
  }

  public Mono<AuthResponse> refreshAccessToken(String refreshTokenValue) {
    return refreshTokenRepository
        .findByToken(refreshTokenValue)
        .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")))
        .flatMap(
            existingToken -> {
              if (Boolean.TRUE.equals(existingToken.getRevoked())) {
                return Mono.error(new RuntimeException("Refresh token already revoked"));
              }
              if (existingToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                return Mono.error(new RuntimeException("Refresh token expired"));
              }
              existingToken.setRevoked(true);
              existingToken.setRevokedAt(LocalDateTime.now());
              return refreshTokenRepository
                  .save(existingToken)
                  .then(userRepository.findByUserUid(existingToken.getUserUid()))
                  .flatMap(
                      user -> {
                        List<String> roles = List.of(UserRole.USER.name());
                        return subscriptionService
                            .resolveEffectivePlan(user.getUserUid())
                            .flatMap(
                                effectivePlan -> {
                                  String newAccessToken =
                                      jwtUtil.generateAccessToken(user.getUserUid(), roles);
                                  String newRefreshTokenValue = jwtUtil.generateRefreshToken();
                                  RefreshToken newRefreshToken =
                                      RefreshToken.builder()
                                          .token(newRefreshTokenValue)
                                          .userUid(user.getUserUid())
                                          .expiresAt(
                                              LocalDateTime.now()
                                                  .plusSeconds(
                                                      jwtUtil.getRefreshTokenExpirationSeconds()))
                                          .createdAt(LocalDateTime.now())
                                          .revoked(false)
                                          .build();
                                  return refreshTokenRepository
                                      .save(newRefreshToken)
                                      .map(
                                          saved ->
                                              new AuthResponse(
                                                  newAccessToken,
                                                  newRefreshTokenValue,
                                                  user.getUserUid(),
                                                  roles,
                                                  effectivePlan,
                                                  Boolean.TRUE.equals(
                                                      user.getOnboardingCompleted()),
                                                  Boolean.TRUE.equals(user.getInitialIncomeSet())));
                                });
                      });
            });
  }

  private Mono<AuthResponse> generateAuthResponse(User user, List<String> roles) {
    String refreshTokenValue = jwtUtil.generateRefreshToken();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .token(refreshTokenValue)
            .userUid(user.getUserUid())
            .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpirationSeconds()))
            .createdAt(LocalDateTime.now())
            .revoked(false)
            .build();
    return refreshTokenRepository
        .save(refreshToken)
        .flatMap(
            saved ->
                subscriptionService
                    .resolveEffectivePlan(user.getUserUid())
                    .map(
                        effectivePlan -> {
                          String accessToken =
                              jwtUtil.generateAccessToken(user.getUserUid(), roles);
                          return new AuthResponse(
                              accessToken,
                              refreshTokenValue,
                              user.getUserUid(),
                              roles,
                              effectivePlan,
                              Boolean.TRUE.equals(user.getOnboardingCompleted()),
                              Boolean.TRUE.equals(user.getInitialIncomeSet()));
                        }));
  }

  public Mono<User> getUserByUid(String userUid) {
    return userRepository.findByUserUid(userUid);
  }

  public Mono<Void> completeOnboarding(String userUid) {
    return userRepository
        .findByUserUid(userUid)
        .flatMap(
            user -> {
              user.setOnboardingCompleted(true);
              user.setInitialIncomeSet(true);
              return userRepository.save(user);
            })
        .then();
  }

  public Mono<Void> logout(String userUid, String refreshTokenValue) {
    return refreshTokenRepository
        .findByToken(refreshTokenValue)
        .flatMap(
            token -> {
              if (!token.getUserUid().equals(userUid)) {
                return Mono.error(new RuntimeException("Invalid token owner"));
              }
              token.setRevoked(true);
              token.setRevokedAt(LocalDateTime.now());
              return refreshTokenRepository.save(token);
            })
        .then();
  }
}
