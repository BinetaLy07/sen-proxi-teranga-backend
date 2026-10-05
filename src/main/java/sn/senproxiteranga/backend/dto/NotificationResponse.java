package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.TypeNotification;

import java.time.LocalDateTime;

// Ce qu'Angular reçoit pour afficher une notification (la cloche 🔔)
public record NotificationResponse(
        Long id,
        TypeNotification type,
        String titre,
        String message,
        // Permet au front d'ouvrir directement la demande concernée (peut être null)
        Long demandeId,
        boolean lue,
        LocalDateTime date) {
}