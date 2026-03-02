package com.alvayonara.finguardriskservice.user.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext {

  public static final String CONTEXT_KEY = "userContext";

  private Long internalUserId;
  private String userUid;
}
