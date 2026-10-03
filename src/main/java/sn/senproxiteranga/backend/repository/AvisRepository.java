package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.Avis;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    // Un avis existe-t-il déjà pour cette demande ? (un seul avis par demande)
    boolean existsByDemandeId(Long demandeId);

    // L'avis d'une demande
    Optional<Avis> findByDemandeId(Long demandeId);

    // L'avis d'une demande, seulement s'il concerne bien ce professionnel (pour sa réponse)
    Optional<Avis> findByDemandeIdAndProfessionnelId(Long demandeId, Long professionnelId);

    // Tous les avis d'un professionnel, du plus récent au plus ancien (page de profil)
    List<Avis> findByProfessionnelIdOrderByCreatedAtDesc(Long professionnelId);

    // Nombre d'avis d'un professionnel
    long countByProfessionnelId(Long professionnelId);

    // Note moyenne d'un professionnel, calculée directement par la base de données
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.professionnel.id = :professionnelId")
    Double calculerMoyenne(@Param("professionnelId") Long professionnelId);
}