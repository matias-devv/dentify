package com.dentify.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Persisted refresh token.
 * One row per issued token; old rows are cleaned up on rotation or logout.
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_token_hash", columnList = "token_hash"),
                @Index(name = "idx_rt_user_id",   columnList = "user_id"),
                @Index(name = "idx_rt_expiry",    columnList = "expiry_date")
        }
)
@Getter @Setter @NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hex of the raw token. Raw value is never persisted. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    /** Set on first persist. */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Updated on every successful verify — useful for audit/debug. */
    private Instant lastUsedAt;

    /**
     * Optimistic lock: protects against two concurrent refresh requests
     * using the same token. The second saveAndFlush will throw
     * OptimisticLockingFailureException, which is caught in the service
     * and mapped to a 401.
     */
    @Version
    private Long version;

    public RefreshToken(String tokenHash, AuthUser user, Instant expiryDate) {
        this.tokenHash = tokenHash;
        this.user      = user;
        this.expiryDate = expiryDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
