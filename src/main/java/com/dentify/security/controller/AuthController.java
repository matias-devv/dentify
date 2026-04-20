package com.dentify.security.controller;

import com.dentify.security.dto.request.AuthLoginRequest;
import com.dentify.security.dto.response.AuthResponse;
import com.dentify.security.dto.response.LoginResult;
import com.dentify.security.dto.response.RefreshResponse;
import com.dentify.security.service.IAuthUserService;
import com.dentify.security.service.IRefreshTokenService;
import com.dentify.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    private final IAuthUserService authService;
    private final IRefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest request, HttpServletResponse response) {

        LoginResult result = authService.loginUser(request);

        String rawRefreshToken = refreshTokenService.createRefreshToken(result.authUser());

        addRefreshTokenCookie(response, rawRefreshToken);

        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity< Map<String, String> > refresh(HttpServletRequest request, HttpServletResponse response) {

        RefreshResponse refreshResponse = refreshTokenService.refresh(request);

        // Rotate: replace the old cookie with the new refresh token
        addRefreshTokenCookie(response, refreshResponse.newRefreshToken());

        // Only the access token goes in the body
        return ResponseEntity.ok( Map.of("accessToken", refreshResponse.accessToken()) );
    }

    // ------------------------------------------------------------------
    // POST /auth/logout
    // ------------------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity< Map<String, String> > logout(HttpServletRequest request, HttpServletResponse response) {

        // Service never throws — cookie is always cleared regardless
        refreshTokenService.logout(request);

        clearRefreshTokenCookie(response);

        return ResponseEntity.ok( Map.of("message", "Logged out successfully."));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String rawToken) {

        ResponseCookie cookie = ResponseCookie.from(RefreshTokenService.REFRESH_COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(this.cookieSecure)
                .path("/auth")
                .maxAge(Duration.ofSeconds(RefreshTokenService.REFRESH_TOKEN_EXPIRY_SEC))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from(RefreshTokenService.REFRESH_COOKIE_NAME, "")
                                              .httpOnly(true)
                                              .secure(this.cookieSecure)
                                              .path("/auth")
                                              .maxAge(Duration.ZERO)
                                              .sameSite("Lax")
                                              .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}