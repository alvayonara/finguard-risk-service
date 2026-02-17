package com.alvayonara.finguardriskservice.subscription;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("subscriptions")
public class Subscription {
    @Id
    private Long id;
    private String userUid;
    private String plan;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
