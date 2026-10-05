package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.Paiement;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Le paiement d'une demande (il n'y en a qu'un au maximum)
    Optional<Paiement> findByDemandeId(Long demandeId);

    // Un paiement a-t-il déjà été enregistré pour cette demande ?
    boolean existsByDemandeId(Long demandeId);

    // Tous les paiements d'un client (ses dépenses), du plus récent au plus ancien
    List<Paiement> findByDemandeClientIdOrderByCreatedAtDesc(Long clientId);

    // Tous les paiements d'un professionnel (ses revenus), du plus récent au plus ancien
    List<Paiement> findByDemandeProfessionnelIdOrderByCreatedAtDesc(Long professionnelId);

    // Pour la règle des 48 h (actions automatiques) :
    // les paiements toujours en attente dont la date limite est dépassée
    List<Paiement> findByStatutAndDateLimiteConfirmationBefore(StatutPaiement statut, LocalDateTime date);

    // Statistiques : la somme des montants des paiements dans ce statut (0 s'il n'y en a aucun)
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.statut = :statut")
    Double sommeDesMontants(@Param("statut") StatutPaiement statut);
}