package com.alvayonara.finguardriskservice.subscription.google;

import java.time.LocalDateTime;

public record GoogleWebhookNotification(
        String eventId,
        String purchaseToken,
        LocalDateTime publishedAt,
        String rawPayload
) {}