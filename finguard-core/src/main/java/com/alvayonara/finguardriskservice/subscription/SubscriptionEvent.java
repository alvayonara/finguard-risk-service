package com.alvayonara.finguardriskservice.subscription;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("subscription_events")
public class SubscriptionEvent {
  @Id private Long id;
  private String platform;
  private String eventId;
  private String externalTransactionId;
  private String type;
  private String jti;
  private LocalDateTime signedAt;
  private String payload;
  private LocalDateTime createdAt;
}
