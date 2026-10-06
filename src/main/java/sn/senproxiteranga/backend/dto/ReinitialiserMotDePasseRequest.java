package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// L'utilisateur a reçu le code par SMS : il le donne avec son nouveau mot de passe
public record ReinitialiserMotDePasseRequest(

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^(\\+221)?(7[05678]|33)\\d{7}$",
                message = "Numéro sénégalais invalide (ex : 771234567 ou +221771234567)")
        String telephone,

        @NotBlank(message = "Le code est obligatoire")
        @Pattern(regexp = "^\\d{6}$", message = "Le code contient 6 chiffres")
        String code,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le nouveau mot de passe doit contenir entre 8 et 72 caractères")
        String nouveauMotDePasse
) {
}