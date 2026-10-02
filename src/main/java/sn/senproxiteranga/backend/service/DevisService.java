package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.DevisRequest;
import sn.senproxiteranga.backend.dto.DevisResponse;

import java.util.List;

/**
 * Gestion des devis : envoi par le professionnel,
 * réponse du client (accepter, refuser, demander une révision)
 * et consultation (devis actuel et historique des versions).
 */
public interface DevisService {

    // ===================== Actions du professionnel =====================

    // Envoie le premier devis d'une demande acceptée
    DevisResponse envoyer(Long professionnelId, Long demandeId, DevisRequest request);

    // Envoie une nouvelle version après une demande de révision du client
    DevisResponse reviser(Long professionnelId, Long demandeId, DevisRequest request);

    // ===================== Actions du client =====================

    // Accepte le devis actuel : la demande passe à DEVIS_ACCEPTE
    DevisResponse accepter(Long clientId, Long demandeId);

    // Refuse le devis actuel : la demande est annulée
    DevisResponse refuser(Long clientId, Long demandeId, String motif);

    // Demande une modification du devis actuel (2 révisions maximum)
    DevisResponse demanderRevision(Long clientId, Long demandeId, String motif);

    // ===================== Consultation =====================

    // Le devis en cours (la version la plus récente)
    DevisResponse trouverActuel(Long demandeId);

    // Toutes les versions du devis, de la plus ancienne à la plus récente
    List<DevisResponse> historique(Long demandeId);
}