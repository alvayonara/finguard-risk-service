package com.alvayonara.finguardriskservice.user.preference;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferenceResponse {
  private String currency;
  private String language;
}
