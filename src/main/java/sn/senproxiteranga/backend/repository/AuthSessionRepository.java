package sn.senproxiteranga.backend.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;

import sn.senproxiteranga.backend.domain.AuthSession;

import java.util.*;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    @EntityGraph(attributePaths = {"utilisateur", "utilisateur.role"})
    Optional<AuthSession> findByAccessHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"utilisateur", "utilisateur.role"})
    Optional<AuthSession> findByRefreshHash(String hash);

    List<AuthSession> findByUtilisateurIdAndRevokedFalse(Long id);
}
