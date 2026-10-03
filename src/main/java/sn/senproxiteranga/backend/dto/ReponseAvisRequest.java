package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * La réponse du professionnel à un avis (une seule fois).
 */
public record ReponseAvisRequest(

        @NotBlank(message = "La réponse ne peut pas être vide")
        @Size(max = 1000, message = "La réponse ne doit pas dépasser 1000 caractères")
        String reponse
) {
}