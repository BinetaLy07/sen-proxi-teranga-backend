package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import sn.senproxiteranga.backend.domain.AuthSession;

import java.util.*;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    @EntityGraph(attributePaths = {"utilisateur", "utilisateur.role"})
    Optional<AuthSession> findByAccessHash(String hash);

    // Renouvellement du badge (POST /api/auth/refresh).
    // "FOR UPDATE" verrouille la ligne pendant la transaction : si deux renouvellements
    // arrivent en même temps avec le même jeton, le second attend le premier.
    // Écrit en SQL natif car MariaDB (XAMPP) refuse le "FOR UPDATE OF ..." que Hibernate
    // générait avec les jointures (erreur SQL 1064). Ce SQL marche sur MariaDB et MySQL.
    // L'utilisateur et son rôle sont chargés ensuite, dans la même transaction.
    @Query(
            value = "SELECT * FROM auth_sessions WHERE refresh_hash = :hash FOR UPDATE",
            nativeQuery = true)
    Optional<AuthSession> findByRefreshHash(@Param("hash") String hash);

    List<AuthSession> findByUtilisateurIdAndRevokedFalse(Long id);
}
