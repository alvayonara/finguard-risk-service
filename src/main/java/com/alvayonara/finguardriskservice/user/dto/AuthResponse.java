package com.alvayonara.finguardriskservice.user.dto;

import java.util.List;

public record AuthResponse(
    String accessToken, String refreshToken, String userUid, List<String> roles) {}
