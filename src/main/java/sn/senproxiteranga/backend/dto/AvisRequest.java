package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * L'avis donné par le client après les travaux.
 */
public record AvisRequest(

        @NotNull(message = "La note est obligatoire")
        @Min(value = 1, message = "La note doit être comprise entre 1 et 5")
        @Max(value = 5, message = "La note doit être comprise entre 1 et 5")
        Integer note,

        // Facultatif
        @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
        String commentaire
) {
}