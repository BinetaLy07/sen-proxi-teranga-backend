package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.RendezVousRequest;
import sn.senproxiteranga.backend.dto.RendezVousResponse;

import java.util.List;

/**
 * Gestion des rendez-vous.
 *
 * Le client choisit la date et l'heure (il propose en premier).
 * Le professionnel accepte, ou propose une autre date ;
 * le client accepte alors cette date, ou en propose encore une autre.
 * Ensuite : reports éventuels, puis suivi des travaux
 * (commencés, terminés, confirmés par le client).
 */
public interface RendezVousService {

    // ===================== Actions du client =====================

    // Propose une date : la première, ou une autre date à la place de celle du professionnel
    RendezVousResponse proposerParClient(Long clientId, Long demandeId, RendezVousRequest request);

    // Accepte la date proposée par le professionnel -> demande PLANIFIEE
    RendezVousResponse accepterParClient(Long clientId, Long demandeId);

    // Change la date d'un rendez-vous déjà accepté (avant le début des travaux)
    RendezVousResponse reporterParClient(Long clientId, Long demandeId, String motif);

    // Confirme que les travaux sont bien terminés -> demande CONFIRMEE
    RendezVousResponse confirmerFinTravaux(Long clientId, Long demandeId);

    // ===================== Actions du professionnel =====================

    // La date du client ne lui convient pas : il propose une autre date
    RendezVousResponse proposerParProfessionnel(Long professionnelId, Long demandeId, RendezVousRequest request);

    // Accepte la date proposée par le client -> demande PLANIFIEE
    RendezVousResponse accepterParProfessionnel(Long professionnelId, Long demandeId);

    // Change la date d'un rendez-vous déjà accepté (avant le début des travaux)
    RendezVousResponse reporterParProfessionnel(Long professionnelId, Long demandeId, String motif);

    // Le jour J : début des travaux -> demande EN_COURS
    RendezVousResponse commencer(Long professionnelId, Long demandeId);

    // Fin des travaux -> demande TERMINEE
    RendezVousResponse terminer(Long professionnelId, Long demandeId);

    // ===================== Consultation =====================

    // Le rendez-vous le plus récent de la demande
    RendezVousResponse trouverActuel(Long demandeId);

    // Tous les rendez-vous de la demande (dates refusées, reportées, acceptée)
    List<RendezVousResponse> historique(Long demandeId);
}
