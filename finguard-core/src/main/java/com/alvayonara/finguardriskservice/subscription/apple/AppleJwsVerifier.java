package com.alvayonara.finguardriskservice.subscription.apple;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AppleJwsVerifier {
  private final ApplePublicKeyProvider keyProvider;
  private final String projectBundleId;

  public AppleJwsVerifier(
      ApplePublicKeyProvider keyProvider, @Value("${apple.bundle-id}") String projectBundleId) {
    this.keyProvider = keyProvider;
    this.projectBundleId = projectBundleId;
  }

  public Mono<SignedJWT> verify(String signedPayload) {
    return keyProvider
        .getKeySet()
        .flatMap(
            jwkSet -> {
              try {
                SignedJWT jwt = SignedJWT.parse(signedPayload);
                String keyId = jwt.getHeader().getKeyID();
                JWK jwk = jwkSet.getKeyByKeyId(keyId);
                if (jwk == null) {
                  return Mono.error(new RuntimeException("Unknown Apple keyId"));
                }
                ECPublicKey publicKey = jwk.toECKey().toECPublicKey();
                JWSVerifier verifier = new ECDSAVerifier(publicKey);
                if (!jwt.verify(verifier)) {
                  return Mono.error(new RuntimeException("Invalid Apple signature"));
                }
                String bundleId = jwt.getJWTClaimsSet().getStringClaim("bundleId");
                if (!projectBundleId.equals(bundleId)) {
                  return Mono.error(new RuntimeException("BundleId mismatch"));
                }
                return Mono.just(jwt);
              } catch (ParseException | JOSEException e) {
                return Mono.error(new RuntimeException("Apple JWS verification failed", e));
              }
            });
  }
}
