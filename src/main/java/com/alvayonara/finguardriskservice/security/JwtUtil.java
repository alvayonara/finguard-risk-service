package com.alvayonara.finguardriskservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JwtUtil {
    @Autowired
    private JwtEncoder jwtEncoder;
    public String generateToken(String userUid, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("finguard-risk-service")
                .subject(userUid)
                .claim("roles", roles)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60 * 60 * 24))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}