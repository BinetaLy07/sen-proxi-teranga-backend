package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

// Une conversation dans la liste : avec qui, le dernier message, et combien de non lus
public record ConversationResponse(
        Long interlocuteurId,
        String interlocuteurNom,
        String interlocuteurRole,          // CLIENT ou PROFESSIONNEL
        String dernierMessage,
        LocalDateTime dateDernierMessage,
        boolean dernierMessageEnvoyeParMoi,  // pour afficher "Vous : ..." devant le texte
        long nombreNonLus
) {
}