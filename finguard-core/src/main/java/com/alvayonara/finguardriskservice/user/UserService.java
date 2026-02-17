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
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private GoogleAuthService googleAuthService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SubscriptionService subscriptionService;

    public Mono<AuthResponse> createOrGetAnonymousUser(String anonymousId) {
        return userRepository
                .findByAnonymousId(anonymousId)
                .switchIfEmpty(
                        userRepository.save(
                                User.builder()
                                        .userUid(IdGenerator.generate(IdPrefix.USER))
                                        .anonymousId(anonymousId)
                                        .plan(UserPlan.FREE.name())
                                        .createdAt(LocalDateTime.now())
                                        .build()))
                .flatMap(user ->
                        generateAuthResponse(
                                user,
                                List.of(UserRole.ANONYMOUS.name())
                        )
                );
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
                .switchIfEmpty(
                        upgradeAnonymousIfExists(
                                anonymousId,
                                googleSub,
                                email,
                                name
                        )
                )
                .flatMap(user ->
                        generateAuthResponse(
                                user,
                                List.of(UserRole.USER.name())
                        )
                );
    }

    public Mono<AuthResponse> refreshAccessToken(String refreshTokenValue) {
        return refreshTokenRepository
                .findByToken(refreshTokenValue)
                .flatMap(refreshToken -> {
                    if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
                        return Mono.error(new RuntimeException("Refresh token revoked"));
                    }
                    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return Mono.error(new RuntimeException("Refresh token expired"));
                    }
                    return userRepository
                            .findByUserUid(refreshToken.getUserUid())
                            .flatMap(user -> {
                                List<String> roles =
                                        user.getGoogleSub() != null
                                                ? List.of(UserRole.USER.name())
                                                : List.of(UserRole.ANONYMOUS.name());
                                return subscriptionService
                                        .resolveEffectivePlan(user.getUserUid())
                                        .map(effectivePlan -> {
                                            String accessToken =
                                                    jwtUtil.generateAccessToken(
                                                            user.getUserUid(),
                                                            roles
                                                    );
                                            return new AuthResponse(
                                                    accessToken,
                                                    refreshTokenValue,
                                                    user.getUserUid(),
                                                    roles,
                                                    effectivePlan
                                            );
                                        });
                            });
                })
                .switchIfEmpty(
                        Mono.error(new RuntimeException("Invalid refresh token"))
                );
    }

    private Mono<AuthResponse> generateAuthResponse(User user, List<String> roles) {
        String accessToken =
                jwtUtil.generateAccessToken(user.getUserUid(), roles);
        String refreshTokenValue =
                jwtUtil.generateRefreshToken();
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(refreshTokenValue)
                        .userUid(user.getUserUid())
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusSeconds(jwtUtil.getRefreshTokenExpirationSeconds()))
                        .createdAt(LocalDateTime.now())
                        .revoked(false)
                        .build();
        return refreshTokenRepository
                .save(refreshToken)
                .flatMap(saved ->
                        subscriptionService
                                .resolveEffectivePlan(user.getUserUid())
                                .map(effectivePlan ->
                                        new AuthResponse(
                                                accessToken,
                                                refreshTokenValue,
                                                user.getUserUid(),
                                                roles,
                                                effectivePlan
                                        )
                                )
                );
    }

    private Mono<User> upgradeAnonymousIfExists(
            String anonymousId,
            String googleSub,
            String email,
            String name) {
        return userRepository
                .findByAnonymousId(anonymousId)
                .flatMap(existingAnon -> {
                    existingAnon.setGoogleSub(googleSub);
                    existingAnon.setEmail(email);
                    existingAnon.setName(name);
                    if (Objects.isNull(existingAnon.getPlan())) {
                        existingAnon.setPlan(UserPlan.FREE.name());
                    }
                    return userRepository.save(existingAnon);
                })
                .switchIfEmpty(
                        userRepository.save(
                                User.builder()
                                        .userUid(IdGenerator.generate(IdPrefix.USER))
                                        .googleSub(googleSub)
                                        .email(email)
                                        .name(name)
                                        .plan(UserPlan.FREE.name())
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        )
                );
    }
    
    public Mono<User> getUserByUid(String userUid) {
        return userRepository.findByUserUid(userUid);
    }
}