package com.dentify.security.dto.response;

import com.dentify.security.model.RefreshToken;

public record RefreshResponse(String accessToken, String newRefreshToken ) {
}
