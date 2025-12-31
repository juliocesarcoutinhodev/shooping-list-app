package br.com.shooping.list.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tb_refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private Long replacedByTokenId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 45)
    private String ip;

    // Construtor
    private RefreshToken(User user, String tokenHash, Instant expiresAt, String userAgent, String ip) {
        validateTokenCreation(user, tokenHash, expiresAt);
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.userAgent = userAgent;
        this.ip = ip;
        this.createdAt = Instant.now();
    }

    // Factory method
    public static RefreshToken create(User user, String tokenHash, Instant expiresAt, String userAgent, String ip) {
        return new RefreshToken(user, tokenHash, expiresAt, userAgent, ip);
    }

    // Validações
    private void validateTokenCreation(User user, String tokenHash, Instant expiresAt) {
        if (user == null) {
            throw new IllegalArgumentException("User não pode ser nulo");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash não pode ser vazio");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Data de expiração não pode ser nula");
        }
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Data de expiração não pode ser no passado");
        }
    }

    // Métodos de negócio
    public void revoke(Long replacedByTokenId) {
        if (this.revokedAt != null) {
            throw new IllegalStateException("Token já foi revogado");
        }
        this.revokedAt = Instant.now();
        this.replacedByTokenId = replacedByTokenId;
    }

    public void markAsUsed() {
        this.lastUsedAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}