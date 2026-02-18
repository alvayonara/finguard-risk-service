package com.alvayonara.finguardriskservice.subscription.apple;

import com.alvayonara.finguardriskservice.subscription.dto.AppleWebhookNotification;
import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionValidationResult;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
public class AppleVerificationService {
    @Autowired
    private AppleJwsVerifier verifier;

    public Mono<SubscriptionValidationResult> verifyLatestByTransaction(String signedTransactionInfo) {
        return verifier.verify(signedTransactionInfo).map(this::extractValidation);
    }

    public Mono<AppleWebhookNotification> parseServerNotification(String signedPayload) {
        return verifier.verify(signedPayload).map(this::extractWebhook);
    }

    private SubscriptionValidationResult extractValidation(SignedJWT jwt) {
        try {
            var claims = jwt.getJWTClaimsSet();
            String productId = claims.getStringClaim("productId");
            long expiryMillis = claims.getLongClaim("expiresDate");
            boolean autoRenew = Boolean.TRUE.equals(claims.getBooleanClaim("autoRenewStatus"));
            boolean canceled = claims.getClaim("revocationDate") != null;
            LocalDateTime expiry =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(expiryMillis),
                            ZoneOffset.UTC
                    );
            return new SubscriptionValidationResult(productId, expiry, autoRenew, canceled);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Apple transaction payload", e);
        }
    }

    private AppleWebhookNotification extractWebhook(SignedJWT jwt) {
        try {
            var outerClaims = jwt.getJWTClaimsSet();
            String notificationUUID = outerClaims.getStringClaim("notificationUUID");
            String notificationType = outerClaims.getStringClaim("notificationType");
            var data = (Map<String, Object>) outerClaims.getClaim("data");
            String signedTransactionInfo = (String) data.get("signedTransactionInfo");
            String signedRenewalInfo = (String) data.get("signedRenewalInfo");


            SignedJWT transactionJwt = SignedJWT.parse(signedTransactionInfo);
            var transactionClaims = transactionJwt.getJWTClaimsSet();
            String productId = transactionClaims.getStringClaim("productId");
            String transactionId = transactionClaims.getStringClaim("transactionId");
            String originalTransactionId = transactionClaims.getStringClaim("originalTransactionId");
            long expiryMillis = transactionClaims.getLongClaim("expiresDate");
            boolean canceled = transactionClaims.getClaim("revocationDate") != null;
            LocalDateTime expiry = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(expiryMillis),
                            ZoneOffset.UTC
                    );

            SignedJWT renewalJwt = SignedJWT.parse(signedRenewalInfo);
            var renewalClaims = renewalJwt.getJWTClaimsSet();
            Boolean autoRenewStatus = renewalClaims.getBooleanClaim("autoRenewStatus");
            boolean autoRenew = autoRenewStatus != null && autoRenewStatus;
            String externalTransactionId = originalTransactionId != null
                            ? originalTransactionId
                            : transactionId;
            SubscriptionValidationResult result = new SubscriptionValidationResult(
                            productId,
                            expiry,
                            autoRenew,
                            canceled
                    );
            return new AppleWebhookNotification(
                    notificationUUID,
                    notificationType,
                    externalTransactionId,
                    result
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid Apple webhook payload", e);
        }
    }
}