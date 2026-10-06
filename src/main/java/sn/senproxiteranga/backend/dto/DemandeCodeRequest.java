package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// "J'ai oublié mon mot de passe" : l'utilisateur donne son numéro de téléphone
public record DemandeCodeRequest(

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^(\\+221)?(7[05678]|33)\\d{7}$",
                message = "Numéro sénégalais invalide (ex : 771234567 ou +221771234567)")
        String telephone
) {
}