package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

// Une conversation dans la liste : avec qui, à propos de quelle demande,
// le dernier message, et combien de non lus.
// Avec la même personne, il y a une conversation PAR demande,
// plus éventuellement une "question générale" (demandeId = null).
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
