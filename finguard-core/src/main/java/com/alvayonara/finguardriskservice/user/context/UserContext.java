package com.alvayonara.finguardriskservice.user.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext {
  private Long internalUserId;
  private String userUid;
}
