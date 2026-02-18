package com.alvayonara.finguardriskservice.subscription;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@Table("subscription_events")
public class SubscriptionEvent {
    @Id
    private Long id;
    private String platform;
    private String eventId;
    private String externalTransactionId;
    private String type;
    private String payload;
    private LocalDateTime createdAt;
}
