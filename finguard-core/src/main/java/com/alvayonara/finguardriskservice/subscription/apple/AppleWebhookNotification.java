package com.alvayonara.finguardriskservice.subscription.apple;

import com.alvayonara.finguardriskservice.subscription.dto.SubscriptionValidationResult;
import java.time.LocalDateTime;

public record AppleWebhookNotification(
    String eventId,
    String type,
    String externalTransactionId,
    String jti,
    LocalDateTime signedAt,
    SubscriptionValidationResult validationResult,
    String rawPayload) {}
