package com.alvayonara.finguardriskservice.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {

  private final GoogleIdTokenVerifier verifier;

  public GoogleAuthService(GoogleIdTokenVerifier verifier) {
    this.verifier = verifier;
  }

  public GoogleIdToken.Payload verify(String idTokenString) {
    try {
      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (Objects.isNull(idToken)) {
        throw new RuntimeException("Invalid Google token");
      }
      return idToken.getPayload();
    } catch (Exception e) {
      throw new RuntimeException("Google verification failed", e);
    }
  }
}
