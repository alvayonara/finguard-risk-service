package com.alvayonara.finguardriskservice.user.preference;

import lombok.Data;

@Data
public class UserPreferenceRequest {
  private String currency;
  private String language;
}
