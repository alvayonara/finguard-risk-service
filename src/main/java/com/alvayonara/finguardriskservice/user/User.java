package com.alvayonara.finguardriskservice.user;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("users")
public class User {
  @Id private Long id;
  private String anonymousId;
  private String googleSub;
  private String email;
  private String name;
  private LocalDateTime createdAt;
}
