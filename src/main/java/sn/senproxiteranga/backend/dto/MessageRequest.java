package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Exemple : {"contenu": "Bonjour, êtes-vous disponible samedi ?"}
public record MessageRequest(

        @NotBlank(message = "Le message est vide")
        @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères")
        String contenu
) {
}