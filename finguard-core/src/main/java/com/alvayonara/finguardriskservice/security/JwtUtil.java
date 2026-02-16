package com.alvayonara.finguardriskservice.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private static final long ACCESS_TOKEN_EXPIRATION = 60 * 15;
  private static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 7;

  @Autowired private JwtEncoder jwtEncoder;

  public String generateAccessToken(String userUid, List<String> roles) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("finguard-risk-service")
            .subject(userUid)
            .claim("roles", roles)
            .claim("type", "access")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ACCESS_TOKEN_EXPIRATION))
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  public long getRefreshTokenExpirationSeconds() {
    return REFRESH_TOKEN_EXPIRATION;
  }
}
