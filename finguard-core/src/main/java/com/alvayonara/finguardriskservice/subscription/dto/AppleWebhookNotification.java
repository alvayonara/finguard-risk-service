package com.alvayonara.finguardriskservice.subscription.dto;

public record AppleWebhookNotification(
        String eventId,
        String type,
        String externalTransactionId,
        SubscriptionValidationResult validationResult
) {}