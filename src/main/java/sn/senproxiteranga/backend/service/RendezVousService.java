package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.RendezVousRequest;
import sn.senproxiteranga.backend.dto.RendezVousResponse;

import java.util.List;

/**
 * Gestion des rendez-vous : proposition de date par le professionnel,
 * réponse du client, reports, puis suivi des travaux
 * (commencés, terminés, confirmés par le client).
 */
public interface RendezVousService {

    // ===================== Actions du professionnel =====================

    // Propose une date pour les travaux (devis accepté obligatoire)
    RendezVousResponse proposer(Long professionnelId, Long demandeId, RendezVousRequest request);

    // Change la date d'un rendez-vous déjà accepté (avant le début des travaux)
    RendezVousResponse reporterParProfessionnel(Long professionnelId, Long demandeId, String motif);

    // Le jour J : début des travaux -> demande EN_COURS
    RendezVousResponse commencer(Long professionnelId, Long demandeId);

    // Fin des travaux -> demande TERMINEE
    RendezVousResponse terminer(Long professionnelId, Long demandeId);

    // ===================== Actions du client =====================

    // Accepte la date proposée -> demande PLANIFIEE
    RendezVousResponse accepter(Long clientId, Long demandeId);

    // Refuse la date proposée : le professionnel devra en proposer une autre
    RendezVousResponse refuser(Long clientId, Long demandeId, String motif);

    // Change la date d'un rendez-vous déjà accepté (avant le début des travaux)
    RendezVousResponse reporterParClient(Long clientId, Long demandeId, String motif);

    // Confirme que les travaux sont bien terminés -> demande CONFIRMEE
    RendezVousResponse confirmerFinTravaux(Long clientId, Long demandeId);

    // ===================== Consultation =====================

    // Le rendez-vous le plus récent de la demande
    RendezVousResponse trouverActuel(Long demandeId);

    // Tous les rendez-vous de la demande (dates refusées, reportées, acceptée)
    List<RendezVousResponse> historique(Long demandeId);
}