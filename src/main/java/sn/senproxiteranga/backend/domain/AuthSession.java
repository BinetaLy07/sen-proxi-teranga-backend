package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "auth_sessions")
public class AuthSession extends BaseEntity {
    @ManyToOne(optional = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true, length = 64)
    private String accessHash;

    @Column(nullable = false, unique = true, length = 64)
    private String refreshHash;

    @Column(nullable = false)
    private Instant accessExpiresAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked;
}
