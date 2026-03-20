package com.dentify.security.repository;

import com.dentify.security.model.AuthUser;
import com.dentify.security.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Single query that loads everything needed for a refresh in one round-trip:
     * user + profile + clinic (for tenantId) + roles + permissions (for authorities).
     */
    @Query("""
        SELECT rt
        FROM RefreshToken rt
        JOIN FETCH rt.user u
        LEFT JOIN FETCH u.userProfile up
        LEFT JOIN FETCH up.clinic c
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH r.permissions
        WHERE rt.tokenHash = :hash
    """)
    Optional<RefreshToken> findByTokenHash(@Param("hash") String hash);

    /**
     * Soft-revoke: used during token rotation so that if the old token is
     * presented again (replay attack), verifyRefreshToken detects it as reuse
     * and can revoke all sessions for the user.
     * Kept as UPDATE (not DELETE) intentionally — detection requires the row
     * to still be present with revoked=true.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllForUser(@Param("user") AuthUser user);

    /**
     * Hard-delete: used on logout. User is done — no reason to keep rows.
     * A presented token after logout returns "not found", which is fine.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteAllForUser(@Param("user") AuthUser user);

    /**
     * Nightly housekeeping: removes all expired rows.
     * Revoked tokens from rotation also expire in ≤7 days, so this
     * keeps the table clean without a separate revoked-only purge.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :cutoff")
    void deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
