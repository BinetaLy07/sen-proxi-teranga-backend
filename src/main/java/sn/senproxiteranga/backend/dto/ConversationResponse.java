package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

// Une conversation dans la liste : avec qui, le dernier message, et combien de non lus.
// UNE conversation par personne (comme WhatsApp) : les questions générales et les messages
// de toutes les demandes avec cette personne sont ensemble.
// demandeId / demandeTitre : la demande dont parle le DERNIER message (null : question générale).
public record ConversationResponse(
        Long interlocuteurId,
        String interlocuteurNom,
        String interlocuteurRole,          // CLIENT ou PROFESSIONNEL
        Long demandeId,                    // null : question générale
        String demandeTitre,               // ex : "Éclairage d'une boutique" (null : question générale)
        String dernierMessage,
        LocalDateTime dateDernierMessage,
        boolean dernierMessageEnvoyeParMoi,  // pour afficher "Vous : ..." devant le texte
        long nombreNonLus
) {
}
