package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

import java.util.List;

@Repository
public interface ProfessionnelRepository extends JpaRepository<Professionnel, Long> {

    /**
     * Recherche des professionnels visibles sur la plateforme.
     * Chaque filtre est facultatif : s'il vaut null, il est ignoré.
     *
     * - metier      : le métier contient ce texte (sans tenir compte des majuscules)
     * - zoneId      : une région, une commune ou un quartier. Le pro est trouvé s'il travaille :
     *                   1. dans cette zone exacte
     *                   2. dans un quartier de cette commune
     *                   3. dans une commune de cette région
     *                   4. dans un quartier d'une commune de cette région
     * - categorieId : le pro a au moins un service actif dans cette catégorie
     *
     * Résultat trié par note moyenne (les meilleurs d'abord).
     */
    @Query("""
            SELECT DISTINCT p FROM Professionnel p
            LEFT JOIN p.zones z
            LEFT JOIN z.commune c
            WHERE p.statutVerification = :valide
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
    List<Professionnel> rechercher(@Param("metier") String metier,
                                   @Param("zoneId") Long zoneId,
                                   @Param("categorieId") Long categorieId,
                                   @Param("valide") StatutVerification valide,
                                   @Param("actif") StatutCompte actif);
}