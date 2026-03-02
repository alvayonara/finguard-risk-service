package com.alvayonara.finguardriskservice.subscription.apple;

import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionValidationResult;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AppleVerificationService {

  private final AppleJwsVerifier verifier;

  public AppleVerificationService(AppleJwsVerifier verifier) {
    this.verifier = verifier;
  }

  public Mono<SubscriptionValidationResult> verifySignedTransaction(String signedTransactionInfo) {
    return verifier.verify(signedTransactionInfo).map(this::mapToValidationResult);
  }

  public Mono<SubscriptionValidationResult> verifyLatestByTransaction(
      String signedTransactionInfo) {
    return verifySignedTransaction(signedTransactionInfo);
  }

  public Mono<AppleWebhookNotification> parseServerNotification(String signedPayload) {
    return verifier
        .verify(signedPayload)
        .flatMap(
            outerJwt -> {
              try {
                var outerClaims = outerJwt.getJWTClaimsSet();
                String eventId = outerClaims.getStringClaim("notificationUUID");
                String type = outerClaims.getStringClaim("notificationType");
                String jti = outerClaims.getJWTID();
                LocalDateTime signedAt =
                    LocalDateTime.ofInstant(outerClaims.getIssueTime().toInstant(), ZoneOffset.UTC);
                Object dataObj = outerClaims.getClaim("data");
                if (dataObj == null) {
                  return Mono.error(new RuntimeException("Missing data claim"));
                }
                @SuppressWarnings("unchecked")
                var data = (java.util.Map<String, Object>) dataObj;
                String signedTransactionInfo = (String) data.get("signedTransactionInfo");
                if (signedTransactionInfo == null) {
                  return Mono.error(new RuntimeException("Missing signedTransactionInfo"));
                }
                return verifySignedTransaction(signedTransactionInfo)
                    .map(
                        result ->
                            new AppleWebhookNotification(
                                eventId,
                                type,
                                extractExternalTransactionId(signedTransactionInfo),
                                jti,
                                signedAt,
                                result,
                                signedPayload));

              } catch (Exception e) {
                return Mono.error(new RuntimeException("Invalid Apple outer JWT", e));
              }
            });
  }

  private SubscriptionValidationResult mapToValidationResult(SignedJWT jwt) {
    try {
      var claims = jwt.getJWTClaimsSet();
      String productId = claims.getStringClaim("productId");
      long expiryMillis = claims.getLongClaim("expiresDate");
      boolean autoRenew =
          claims.getBooleanClaim("autoRenewStatus") != null
              && claims.getBooleanClaim("autoRenewStatus");
      boolean canceled = claims.getClaim("revocationDate") != null;
      LocalDateTime expiry =
          LocalDateTime.ofInstant(Instant.ofEpochMilli(expiryMillis), ZoneOffset.UTC);
      return new SubscriptionValidationResult(productId, expiry, autoRenew, canceled);
    } catch (Exception e) {
      throw new RuntimeException("Invalid Apple signed transaction", e);
    }
  }

  private String extractExternalTransactionId(String signedTransactionInfo) {
    try {
      SignedJWT jwt = SignedJWT.parse(signedTransactionInfo);
      var claims = jwt.getJWTClaimsSet();
      String originalTransactionId = claims.getStringClaim("originalTransactionId");
      String transactionId = claims.getStringClaim("transactionId");
      return originalTransactionId != null ? originalTransactionId : transactionId;
    } catch (Exception e) {
      throw new RuntimeException("Cannot extract transaction ID", e);
    }
  }
}
