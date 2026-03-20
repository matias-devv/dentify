package com.dentify.security.service;

import com.dentify.common.util.TokenUtils;
import com.dentify.security.authority.AuthorityResolver;
import com.dentify.common.util.JwtUtils;
import com.dentify.exception.auth.RefreshTokenException;
import com.dentify.security.dto.response.RefreshResponse;
import com.dentify.security.model.AuthUser;
import com.dentify.security.model.RefreshToken;
import com.dentify.security.repository.IRefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

/**
 * Handles the full lifecycle of refresh tokens:
 *   create → verify → rotate → revoke
 *
 * Design decisions:
 *  - Token value = 32 bytes from SecureRandom, hex-encoded (64 chars). No JWT
 *    structure — the DB is the source of truth for validity.
 *  - Rotation: every successful refresh issues a new token and immediately
 *    revokes the old one, limiting the window for replay attacks.
 *  - Revocation: a simple `revoked` flag; deleted tokens are also handled
 *    gracefully so the two can coexist.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {

    public static final String REFRESH_COOKIE_NAME      = "refreshToken";
    public static final long   REFRESH_TOKEN_EXPIRY_SEC = 7L * 24 * 60 * 60; // 7 days

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IAuthUserService        authUserService;       // only for resolveTenant
    private final AuthorityResolver       authorityResolver;
    private final JwtUtils                jwtUtils;

    /**
     * Creates and persists a refresh token for the given user.
     * Returns the RAW token — this is the only time it leaves the server.
     * The hash is what gets stored in the DB.
     */
    @Override
    public String createRefreshToken(AuthUser user) {

        String raw  = generateSecureToken();

        String hash = TokenUtils.sha256Hex(raw);

        refreshTokenRepository.save( new RefreshToken( hash, user, Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY_SEC) ) );

        return raw;
    }

    /**
     * Full refresh flow:
     *   read cookie → verify → rotate → issue new access token + new refresh cookie value.
     */
    @Override
    public RefreshResponse refresh(HttpServletRequest request) {

        String rawToken = extractRefreshTokenFromCookie(request).orElseThrow(() -> new RefreshTokenException("Refresh token cookie is missing."));

        RefreshToken verified = verifyRefreshToken(rawToken);

        String newRawToken = rotateRefreshToken(verified);

        // Build Authentication from the already-loaded user (no extra DB call)
        AuthUser user = verified.getUser();

        List<GrantedAuthority> authorities = authorityResolver.resolveAuthorities(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        // resolveTenant handles ADMIN (platform tenant) and clinic users correctly
        String tenantId    = authUserService.resolveClinicTenant(user);

        String accessToken = jwtUtils.createToken(auth, tenantId);

        return new RefreshResponse(accessToken, newRawToken);
    }

    /**
     * Logout: hard-delete all tokens for this user, silently ignore missing/invalid cookie.
     * The controller clears the cookie regardless — this method must never throw.
     */
    @Override
    public void logout(HttpServletRequest request) {

        extractRefreshTokenFromCookie(request).ifPresent(rawToken -> {

            String hash = TokenUtils.sha256Hex(rawToken);

            refreshTokenRepository.findByTokenHash(hash)
                                  .ifPresent(rt -> refreshTokenRepository.deleteAllForUser(rt.getUser()));
        });
    }

    /**
     * Validates a raw token.
     *
     * Order matters:
     *   1. Check revoked FIRST — a revoked token means it was already rotated.
     *      Presenting it again = replay attack. Revoke all sessions immediately.
     *   2. Check expired after — expired-only is a normal session end, not an attack.
     */
    private RefreshToken verifyRefreshToken(String rawToken) {

        String hash = TokenUtils.sha256Hex(rawToken);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found."));

        if (rt.isRevoked()) {
            // Token was already rotated — someone is replaying it.
            // Revoke every session for this user and force re-login.
            refreshTokenRepository.revokeAllForUser(rt.getUser());
            log.warn("Refresh token reuse detected for user '{}'. All sessions revoked.",
                    rt.getUser().getUsername());
            throw new RefreshTokenException("Session invalidated. Please log in again.");
        }

        if (rt.isExpired()) {
            throw new RefreshTokenException("Refresh token has expired. Please log in again.");
        }

        rt.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(rt);

        return rt;
    }

    /**
     * Rotation:
     *   1. Soft-revoke the old token (keep row for reuse detection).
     *   2. saveAndFlush forces the @Version check — if two concurrent requests
     *      raced here, the second throws OptimisticLockingFailureException → 401.
     *   3. Issue a fresh token.
     *
     * Returns the raw token string (hash is persisted, raw goes to the cookie).
     */
    public String rotateRefreshToken(RefreshToken oldToken) {

        oldToken.setRevoked(true);
        oldToken.setLastUsedAt(Instant.now());

        try {
            refreshTokenRepository.saveAndFlush(oldToken);
        } catch (OptimisticLockingFailureException e) {
            // Two simultaneous refresh requests with the same token.
            // Treat as suspicious — both fail, user re-authenticates.
            throw new RefreshTokenException("Concurrent session conflict. Please log in again.");
        }

        String raw  = generateSecureToken();

        String hash = TokenUtils.sha256Hex(raw);

        refreshTokenRepository.save(new RefreshToken(hash, oldToken.getUser(), Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY_SEC)));

        return raw;
    }

    /** Nightly purge of expired rows (includes expired revoked tokens from rotation). */
    @Scheduled(cron = "0 0 2 * * *")
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteExpiredBefore(Instant.now());
    }

    // Helpers
    private String generateSecureToken() {

        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                     .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
                     .map(Cookie::getValue)
                     .findFirst();
    }
}