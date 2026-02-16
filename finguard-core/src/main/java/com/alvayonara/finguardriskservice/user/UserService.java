package com.alvayonara.finguardriskservice.user;

import com.alvayonara.finguardriskservice.common.id.IdGenerator;
import com.alvayonara.finguardriskservice.common.id.IdPrefix;
import com.alvayonara.finguardriskservice.security.GoogleAuthService;
import com.alvayonara.finguardriskservice.security.JwtUtil;
import com.alvayonara.finguardriskservice.user.dto.AuthResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  @Autowired private UserRepository userRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private GoogleAuthService googleAuthService;
  @Autowired private JwtUtil jwtUtil;

  public Mono<AuthResponse> createOrGetAnonymousUser(String anonymousId) {
    return userRepository
        .findByAnonymousId(anonymousId)
        .switchIfEmpty(
            userRepository.save(
                User.builder()
                    .userUid(IdGenerator.generate(IdPrefix.USER))
                    .anonymousId(anonymousId)
                    .createdAt(LocalDateTime.now())
                    .build()))
        .flatMap(
            user -> {
              List<String> eligibleRole = List.of(UserRole.ANONYMOUS.name());
              return generateAuthResponse(user.getUserUid(), eligibleRole);
            });
  }

  public Mono<AuthResponse> loginWithGoogle(String idToken, String anonymousId) {
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
        .switchIfEmpty(upgradeAnonymousIfExists(anonymousId, googleSub, email, name))
        .flatMap(
            user -> {
              List<String> eligibleRole = List.of(UserRole.USER.name());
              return generateAuthResponse(user.getUserUid(), eligibleRole);
            });
  }

  public Mono<AuthResponse> refreshAccessToken(String refreshTokenValue) {
    return refreshTokenRepository
        .findByToken(refreshTokenValue)
        .flatMap(
            refreshToken -> {
              if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
                return Mono.error(new RuntimeException("Refresh token has been revoked"));
              }
              if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                return Mono.error(new RuntimeException("Refresh token has expired"));
              }
              return userRepository
                  .findByUserUid(refreshToken.getUserUid())
                  .flatMap(
                      user -> {
                        List<String> roles =
                            user.getGoogleSub() != null
                                ? List.of(UserRole.USER.name())
                                : List.of(UserRole.ANONYMOUS.name());
                        String accessToken = jwtUtil.generateAccessToken(user.getUserUid(), roles);
                        return Mono.just(
                            new AuthResponse(
                                accessToken, refreshTokenValue, user.getUserUid(), roles));
                      });
            })
        .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")));
  }

  private Mono<AuthResponse> generateAuthResponse(String userUid, List<String> roles) {
    String accessToken = jwtUtil.generateAccessToken(userUid, roles);
    String refreshTokenValue = jwtUtil.generateRefreshToken();

    RefreshToken refreshToken =
        RefreshToken.builder()
            .token(refreshTokenValue)
            .userUid(userUid)
            .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpirationSeconds()))
            .createdAt(LocalDateTime.now())
            .revoked(false)
            .build();

    return refreshTokenRepository
        .save(refreshToken)
        .map(saved -> new AuthResponse(accessToken, refreshTokenValue, userUid, roles));
  }

  private Mono<User> upgradeAnonymousIfExists(
      String anonymousId, String googleSub, String email, String name) {
    return userRepository
        .findByAnonymousId(anonymousId)
        .flatMap(
            existingAnon -> {
              existingAnon.setGoogleSub(googleSub);
              existingAnon.setEmail(email);
              existingAnon.setName(name);
              return userRepository.save(existingAnon);
            })
        .switchIfEmpty(
            userRepository.save(
                User.builder()
                    .userUid(IdGenerator.generate(IdPrefix.USER))
                    .googleSub(googleSub)
                    .email(email)
                    .name(name)
                    .createdAt(LocalDateTime.now())
                    .build()));
  }
}
