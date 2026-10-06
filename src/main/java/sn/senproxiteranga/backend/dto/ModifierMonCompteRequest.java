package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Ce que l'utilisateur connecté peut modifier dans « Mon compte ».
// L'email ne change pas ici : il sert à se connecter.
public record ModifierMonCompteRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^(\\+221)?(7[05678]|33)\\d{7}$",
                message = "Numéro sénégalais invalide (ex : 771234567 ou +221771234567)")
        String telephone,

        // Facultatifs
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse,

        Long zoneId
) {
}