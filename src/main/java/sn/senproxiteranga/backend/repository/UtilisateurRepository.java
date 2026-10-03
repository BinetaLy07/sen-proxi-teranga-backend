package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByIdAndRoleNom(Long id, NomRole nomRole);

    boolean existsByIdAndRoleNom(Long id, NomRole nomRole);

    // Pour la connexion : retrouver un compte par son email
    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    // Pour l'inscription : cet email est-il déjà utilisé ?
    boolean existsByEmailIgnoreCase(String email);

    // Pour l'inscription : ce téléphone est-il déjà utilisé ?
    boolean existsByTelephone(String telephone);
    @Query("""
            SELECT DISTINCT p FROM Utilisateur p
            LEFT JOIN p.zones z
            LEFT JOIN z.commune c
            WHERE p.role.nom = sn.senproxiteranga.backend.domain.enums.NomRole.PROFESSIONNEL
              AND p.statutVerification = :valide
              AND p.statutCompte = :actif
              AND (:metier IS NULL OR LOWER(p.metier) LIKE LOWER(CONCAT('%', :metier, '%')))
              AND (:zoneId IS NULL
                   OR z.id = :zoneId
                   OR c.id = :zoneId
                   OR z.region.id = :zoneId
                   OR c.region.id = :zoneId)
              AND (:categorieId IS NULL OR EXISTS (
                    SELECT s.id FROM ServiceProfessionnel s
                    WHERE s.professionnel = p
                      AND s.actif = true
                      AND s.categorie.id = :categorieId))
            ORDER BY p.noteMoyenne DESC
            """)
    List<Utilisateur> rechercher(@Param("metier") String metier,
                                   @Param("zoneId") Long zoneId,
                                   @Param("categorieId") Long categorieId,
                                   @Param("valide") StatutVerification valide,
                                   @Param("actif") StatutCompte actif);
}
