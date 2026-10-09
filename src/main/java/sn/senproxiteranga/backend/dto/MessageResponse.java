package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long expediteurId,
        String expediteurNom,
        Long destinataireId,
        String destinataireNom,
        String contenu,         // message supprimé : « Ce message a été supprimé »
                                // (l'administrateur, lui, voit le texte d'origine)
        LocalDateTime dateEnvoi,
        boolean lu,
        Long demandeId,         // la demande dont on parle (null : question générale)
        String type,            // "TEXTE" ou "AUDIO"
        Integer audioDuree,     // durée du vocal en secondes (null pour un texte)
        // Le son d'un vocal se récupère avec GET /api/messages/{id}/audio
        boolean supprime,       // l'expéditeur a supprimé ce message
        LocalDateTime dateSuppression
) {
}
