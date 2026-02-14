package com.alvayonara.finguardriskservice.user.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String idToken, String anonymousId) {}
