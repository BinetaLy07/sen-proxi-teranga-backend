package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * La date proposée par le professionnel pour réaliser les travaux.
 * Format JSON : "2026-10-10T09:00:00" (10 octobre 2026 à 9 h).
 */
public record RendezVousRequest(

        @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
        @Future(message = "Le rendez-vous doit être fixé à une date future")
        LocalDateTime dateHeure
) {
}