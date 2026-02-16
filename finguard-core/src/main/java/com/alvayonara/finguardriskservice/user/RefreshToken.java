package com.alvayonara.finguardriskservice.user;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("refresh_tokens")
public class RefreshToken {
  @Id private Long id;
  private String token;
  private String userUid;
  private LocalDateTime expiresAt;
  private LocalDateTime createdAt;
  private Boolean revoked;
  private LocalDateTime revokedAt;
}
