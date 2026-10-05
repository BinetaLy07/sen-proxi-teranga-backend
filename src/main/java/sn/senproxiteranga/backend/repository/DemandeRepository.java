package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DemandeRepository extends JpaRepository<Demande, Long> {

    // Les demandes d'un client, les plus récentes d'abord (suivi côté client)
    List<Demande> findByClientIdOrderByCreatedAtDesc(Long clientId);

    // Les demandes reçues par un professionnel : les URGENTES d'abord, puis les plus récentes
    List<Demande> findByProfessionnelIdOrderByUrgenteDescCreatedAtDesc(Long professionnelId);

    // Pareil, filtrées par statut (ex : seulement les CREEE), urgentes d'abord
    List<Demande> findByProfessionnelIdAndStatutOrderByUrgenteDescCreatedAtDesc(Long professionnelId, StatutDemande statut);

    // Une demande précise, à condition qu'elle appartienne à ce client
    Optional<Demande> findByIdAndClientId(Long id, Long clientId);

    // Une demande précise, à condition qu'elle soit adressée à ce professionnel
    Optional<Demande> findByIdAndProfessionnelId(Long id, Long professionnelId);

    // Ce service a-t-il déjà des demandes ? (pour empêcher sa suppression)
    boolean existsByServiceId(Long serviceId);

    // Pour la règle des 48 h (actions automatiques) :
    // les demandes toujours dans ce statut dont la date d'expiration est dépassée
    List<Demande> findByStatutAndDateExpirationBefore(StatutDemande statut, LocalDateTime date);

    // ----- Administration -----

    // Toutes les demandes d'un statut, les plus anciennes d'abord (ex : les litiges à traiter)
    List<Demande> findByStatutOrderByUpdatedAtAsc(StatutDemande statut);

    // Nombre de demandes dans un statut (statistiques)
    long countByStatut(StatutDemande statut);
}