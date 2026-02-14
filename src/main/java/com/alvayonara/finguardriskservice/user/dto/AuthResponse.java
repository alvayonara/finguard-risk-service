package com.alvayonara.finguardriskservice.user.dto;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String userUid,
        List<String> roles
) {
}