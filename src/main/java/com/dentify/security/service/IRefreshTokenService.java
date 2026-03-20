package com.dentify.security.service;

import com.dentify.security.dto.response.RefreshResponse;
import com.dentify.security.model.AuthUser;
import com.dentify.security.model.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;

public interface IRefreshTokenService {

    /** Creates a new refresh token for the user. Returns the raw value for the cookie. */
    String createRefreshToken(AuthUser user);

    /** Full refresh flow: verify → rotate → new access token. */
    RefreshResponse refresh(HttpServletRequest request);

    /** Revokes all tokens for the user. Never throws. */
    void logout(HttpServletRequest request);
}
